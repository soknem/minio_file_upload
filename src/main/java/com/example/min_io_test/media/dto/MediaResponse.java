package com.example.min_io_test.media.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record MediaResponse(
        String name,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String contentType,
        String extension,
        String uri,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long size
) {

}
