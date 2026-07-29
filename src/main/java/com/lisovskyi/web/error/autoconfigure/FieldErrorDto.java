package com.lisovskyi.web.error.autoconfigure;

import lombok.Builder;

@Builder
public record FieldErrorDto(
    String field, 
    Object rejectedValue, 
    String message) 
{}
