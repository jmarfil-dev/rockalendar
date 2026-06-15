package com.jmarfildev.rockalendar.common.storage;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.StorageException;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OgImageScraperService {

    private static final int TIMEOUT_MS = 10_000;
    private static final long MAX_IMAGE_BYTES = 20L * 1024 * 1024;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(TIMEOUT_MS))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Descarga la página indicada, extrae la URL de imagen social y devuelve los bytes de esa imagen.
     * El llamador debe pasar los bytes por {@link ImageProcessingService#process(byte[])} antes de subirlos.
     *
     * @throws StorageException si la URL no es válida, la página no tiene imagen social, o la imagen no es descargable
     */
    public byte[] scrape(String sourceUrl) {
        validateHttpUrl(sourceUrl);

        // URL directa del CDN de Facebook obtenida con "copiar dirección de imagen"
        if (isFbcdnUrl(sourceUrl)) {
            return downloadImage(sourceUrl);
        }

        // El embed de Instagram es SSR y expone srcset con la resolución real;
        // el HTML estático del post solo da og:image a 640x640
        if (isInstagramPost(sourceUrl)) {
            String imageUrl = tryInstagramEmbed(sourceUrl);
            if (imageUrl != null) return downloadImage(imageUrl);
        }

        // mbasic.facebook.com es SSR y expone directamente el img src del CDN fbcdn;
        // si falla, se intenta og:image directamente. Facebook bloquea el acceso sin login,
        // así que en ese caso se lanza un error específico que explica qué alternativa usar.
        if (isFacebookUrl(sourceUrl)) {
            String imageUrl = tryFacebookMbasic(sourceUrl);
            if (imageUrl != null) return downloadImage(imageUrl);
            try {
                Document doc = fetchDocument(sourceUrl);
                String ogImageUrl = extractImageUrl(doc, sourceUrl);
                return downloadImage(ogImageUrl);
            } catch (StorageException e) {
                if (ErrorConstants.SCRAPE_NO_OG_IMAGE.equals(e.getCode())) {
                    throw new StorageException(ErrorConstants.SCRAPE_FACEBOOK_BLOCKED);
                }
                throw e;
            }
        }

        Document doc = fetchDocument(sourceUrl);
        String imageUrl = extractImageUrl(doc, sourceUrl);
        return downloadImage(imageUrl);
    }

    private void validateHttpUrl(String url) {
        try {
            String scheme = URI.create(url).getScheme();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                throw new StorageException(ErrorConstants.SCRAPE_INVALID_URL);
            }
        } catch (IllegalArgumentException e) {
            throw new StorageException(ErrorConstants.SCRAPE_INVALID_URL);
        }
    }

    // Jsoup gestiona gzip, charset y redirects automáticamente
    private Document fetchDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .timeout(TIMEOUT_MS)
                    .maxBodySize(5 * 1024 * 1024)
                    .followRedirects(true)
                    .get();
        } catch (HttpStatusException e) {
            log.warn("scraping fallido url={} status={}", url, e.getStatusCode());
            throw new StorageException(ErrorConstants.SCRAPE_UNREACHABLE);
        } catch (IOException e) {
            log.warn("scraping fallido url={}: {}", url, e.getMessage());
            throw new StorageException(ErrorConstants.SCRAPE_UNREACHABLE);
        }
    }

    private boolean isFbcdnUrl(String url) {
        try {
            String host = URI.create(url).getHost();
            return host != null && host.endsWith(".fbcdn.net");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isInstagramPost(String url) {
        return url.contains("instagram.com/p/");
    }

    private boolean isFacebookUrl(String url) {
        return url.contains("facebook.com");
    }

    private String tryFacebookMbasic(String sourceUrl) {
        try {
            String mbasicUrl = toFacebookMbasicUrl(sourceUrl);
            Document doc = fetchDocument(mbasicUrl);
            log.debug("facebook mbasic: título='{}' imgs={}", doc.title(), doc.select("img").size());

            for (String attr : List.of("src", "data-src")) {
                Element img = doc.select("img[" + attr + "*=fbcdn]").first();
                if (img == null) img = doc.select("img[" + attr + "*=scontent]").first();
                if (img != null) {
                    String url = img.attr(attr);
                    if (!url.isBlank()) {
                        log.debug("imagen facebook extraída de mbasic url={}", url);
                        return url;
                    }
                }
            }
            log.debug("facebook mbasic: no se encontró imagen fbcdn/scontent en '{}'", mbasicUrl);
        } catch (StorageException e) {
            log.debug("facebook mbasic fallido, se intentará extracción estándar: {}", e.getMessage());
        }
        return null;
    }

    private String toFacebookMbasicUrl(String url) {
        // mbasic usa el formato antiguo photo.php?fbid= y solo necesita ese parámetro
        String mbasic = url
                .replace("www.facebook.com", "mbasic.facebook.com")
                .replace("m.facebook.com", "mbasic.facebook.com")
                .replace("/photo?", "/photo.php?");

        // Elimina parámetros que mbasic no entiende (set=, locale=, etc.), deja solo fbid=
        try {
            URI uri = URI.create(mbasic);
            if (uri.getQuery() != null) {
                String fbid = java.util.Arrays.stream(uri.getQuery().split("&"))
                        .filter(p -> p.startsWith("fbid="))
                        .findFirst()
                        .orElse(null);
                if (fbid != null) {
                    mbasic = uri.getScheme() + "://" + uri.getHost() + uri.getPath() + "?" + fbid;
                }
            }
        } catch (Exception ignored) {}

        return mbasic;
    }

    // El embed no necesita JS y expone el srcset real en el HTML inicial
    private String tryInstagramEmbed(String sourceUrl) {
        try {
            String embedUrl = toInstagramEmbedUrl(sourceUrl);
            Document embedDoc = fetchDocument(embedUrl);
            String url = extractLargestFromSrcset(embedDoc);
            if (url != null) {
                log.debug("imagen instagram extraída del embed url={}", url);
                return url;
            }
        } catch (StorageException e) {
            log.debug("instagram embed fallido, se intentará extracción estándar: {}", e.getMessage());
        }
        return null;
    }

    private String toInstagramEmbedUrl(String url) {
        String base = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
        if (!base.endsWith("/")) base += "/";
        return base.contains("/embed/") ? base : base + "embed/";
    }

    // Parsea el srcset y devuelve la URL de mayor anchura
    private String extractLargestFromSrcset(Document doc) {
        for (Element img : doc.select("img[srcset]")) {
            String srcset = img.attr("srcset");
            if (srcset.isBlank()) continue;

            String bestUrl = null;
            int bestWidth = 0;
            for (String entry : srcset.split(",")) {
                String[] parts = entry.trim().split("\\s+");
                if (parts.length < 2) continue;
                try {
                    int width = Integer.parseInt(parts[parts.length - 1].replace("w", ""));
                    if (width > bestWidth) {
                        bestWidth = width;
                        bestUrl = parts[0];
                    }
                } catch (NumberFormatException ignored) {}
            }
            if (bestUrl != null) return bestUrl;
        }
        return null;
    }

    private String extractImageUrl(Document doc, String baseUrl) {
        // JSON-LD primero: Instagram y otros sitios embeben la imagen a resolución original aquí
        String url = extractFromJsonLd(doc);
        if (url != null) return url;

        url = doc.select("meta[property=og:image:secure_url]").attr("content");
        if (!url.isBlank()) return url;

        url = doc.select("meta[property=og:image]").attr("content");
        if (!url.isBlank()) return url;

        url = doc.select("meta[name=twitter:image]").attr("content");
        if (!url.isBlank()) return url;

        log.debug("imagen social no encontrada en url={}", baseUrl);
        throw new StorageException(ErrorConstants.SCRAPE_NO_OG_IMAGE);
    }

    // Instagram embebe en JSON-LD el array "image" con la URL a resolución original (ej. 1080x1350)
    // en vez del thumbnail 640x640 que expone og:image
    private String extractFromJsonLd(Document doc) {
        for (Element script : doc.select("script[type=application/ld+json]")) {
            try {
                JsonNode root = objectMapper.readTree(script.data());
                JsonNode node = root.isArray() ? root.get(0) : root;
                if (node == null) continue;

                JsonNode images = node.path("image");
                if (images.isArray() && !images.isEmpty()) {
                    JsonNode first = images.get(0);
                    String url = first.isObject() ? first.path("url").asText("") : first.asText("");
                    if (!url.isBlank()) return url;
                } else if (images.isTextual()) {
                    String url = images.asText("");
                    if (!url.isBlank()) return url;
                }
            } catch (Exception e) {
                // JSON-LD malformado o con estructura inesperada, continuamos con el siguiente
            }
        }
        return null;
    }

    private byte[] downloadImage(String imageUrl) {
        validateHttpUrl(imageUrl);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofMillis(TIMEOUT_MS))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() >= 400) {
                log.warn("descarga de imagen fallida url={} status={}", imageUrl, response.statusCode());
                throw new StorageException(ErrorConstants.SCRAPE_UNREACHABLE);
            }

            byte[] body = response.body();
            if (body.length > MAX_IMAGE_BYTES) {
                throw new StorageException(ErrorConstants.INVALID_IMAGE);
            }

            return body;
        } catch (StorageException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StorageException(ErrorConstants.SCRAPE_UNREACHABLE);
        } catch (IOException e) {
            log.warn("descarga de imagen fallida url={}: {}", imageUrl, e.getMessage());
            throw new StorageException(ErrorConstants.SCRAPE_UNREACHABLE);
        }
    }
}
