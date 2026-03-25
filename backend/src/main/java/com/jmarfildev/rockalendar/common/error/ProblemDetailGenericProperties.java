package com.jmarfildev.rockalendar.common.error;

import java.net.URI;
import java.time.OffsetDateTime;

import org.springframework.http.ProblemDetail;

/**
 * @author jmarfil
 *
 */
public class ProblemDetailGenericProperties {
    private ProblemDetailGenericProperties() {}

    public static ProblemDetail setGenericProperties(ProblemDetail pd,
                                                     String title,
                                                     String code,
                                                     String instance,
                                                     String type) {
        pd.setTitle(title);
        pd.setInstance(URI.create(instance == null ? "" : instance));
        pd.setType(URI.create("urn:rockalendar:error:%s".formatted(type)));
        pd.setProperty("timestamp", OffsetDateTime.now());
        pd.setProperty("code", code);
        return pd;
    }
}
