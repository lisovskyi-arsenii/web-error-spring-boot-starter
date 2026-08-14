package com.lisovskyi.web.error.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int status,
    String code,
    String message,
    Instant timestamp,
    String path,
    List<FieldErrorDto> fieldErrors,
    String stackTrace
) {}
