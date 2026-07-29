package com.lisovskyi.web.error.autoconfigure.standard;

import com.lisovskyi.web.error.autoconfigure.base.AppException;
import org.springframework.http.HttpStatus;

public class ForbiddenOperationException extends AppException {
    public ForbiddenOperationException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN_OPERATION");
    }
}
