package com.jmarfildev.rockalendar.common.error;

import java.net.URI;
import java.time.OffsetDateTime;

import org.springframework.http.ProblemDetail;

/**
 * @author jmarfil
 *
 */
public class ProblemDetailGenericProperties {
    public static ProblemDetail setGenericProperties(ProblemDetail pd, String status, String message, String instance, String type) {
        pd.setTitle(status);
        pd.setDetail(message);
        pd.setInstance(URI.create(instance));
        pd.setType(URI.create("urn:rockalendar:error:%s".formatted(type != null ? type : status.toLowerCase().replaceAll("\\s+", "-"))));
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }
}
