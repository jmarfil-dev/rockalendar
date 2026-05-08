package com.jmarfildev.rockalendar.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.jmarfildev.rockalendar.common.doc.PageMetadataDoc;

/**
 * @author jmarfil
 *
 */
public record PageResponse<T>(List<T> content, PageMetadataDoc page) {

    public static <T> PageResponse<T> of(Page<T> springPage) {
        return new PageResponse<>(springPage.getContent(), new PageMetadataDoc(springPage.getSize(), springPage.getNumber(),
                                                                               springPage.getTotalElements(), springPage.getTotalPages()));
    }
}
