package com.jmarfildev.rockalendar.common.storage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

import com.jmarfildev.rockalendar.common.Constants;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.StorageException;

/**
 * @author jmarfil
 *
 */
@Service
public class ImageProcessingService {

    private static final int MAX_DIMENSION = 1200;
    private static final float JPEG_QUALITY = 0.85f;

    /**
     * Valida y procesa un fichero de imagen: comprueba que sea una imagen válida
     * y la redimensiona a máximo 1200px, convirtiéndola a JPEG.
     * El límite de tamaño lo aplica Tomcat (10 MB) antes de llegar aquí.
     *
     * @throws StorageException si el fichero no es una imagen válida
     */
    public byte[] process(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException(ErrorConstants.INVALID_IMAGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(Constants.IMAGE_CONTENT_TYPE_PREFIX)) {
            throw new StorageException(ErrorConstants.INVALID_IMAGE);
        }

        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img == null) {
                throw new StorageException(ErrorConstants.INVALID_IMAGE);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            var builder = Thumbnails.of(img).outputFormat(Constants.IMAGE_OUTPUT_FORMAT).outputQuality(JPEG_QUALITY);

            if (img.getWidth() > MAX_DIMENSION || img.getHeight() > MAX_DIMENSION) {
                builder.size(MAX_DIMENSION, MAX_DIMENSION).keepAspectRatio(true);
            } else {
                builder.scale(1.0);
            }

            builder.toOutputStream(out);
            return out.toByteArray();
        } catch (StorageException e) {
            throw e;
        } catch (IOException e) {
            throw new StorageException(ErrorConstants.INVALID_IMAGE, e);
        }
    }
}
