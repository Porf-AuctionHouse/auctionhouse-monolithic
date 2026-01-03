package com.example.monoauction.common.execptions;

import com.example.monoauction.authentication.model.enums.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuctionHouseException.class)
    public ResponseEntity<ApiError> handleCrmException(AuctionHouseException exception) {
        final ErrorMessage errorMessage = exception.getErrorMessage();
        final HttpStatus status = ErrorMessage.getHttpStatus(errorMessage);
        final ApiError body = createApiError(errorMessage);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(value = {
            MethodArgumentTypeMismatchException.class,
    })

    public ResponseEntity<ApiError> handleExceptions(MethodArgumentTypeMismatchException exception) {
        final ErrorMessage errorMessage = ErrorMessage.BAD_REQUEST;
        final HttpStatus status = ErrorMessage.getHttpStatus(errorMessage);

        final String paramName = exception.getName();

        String requiredTypeName = "unknown";
        if (exception.getRequiredType() != null) {
            requiredTypeName = exception.getRequiredType().getSimpleName();
        }

        String providedTypeName = "unknown";
        if (exception.getValue() != null) {
            providedTypeName = exception.getValue().getClass().getSimpleName();
        }

        final String message = String.format(
                "Invalid value for parameter '%s'. Expected type: '%s', but got: '%s'.",
                paramName, requiredTypeName, providedTypeName
        );

        final ApiError body = createApiError(errorMessage, message);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException exception) {
        final Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach((error) -> {
                    final String fieldName = error.getField();
                    final String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        final ErrorMessage errorMessage = ErrorMessage.BAD_REQUEST;
        final HttpStatus status = ErrorMessage.getHttpStatus(errorMessage);
        final ApiError body = new ApiError(errorMessage, errors.toString());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception exception) {
        final ErrorMessage errorMessage = ErrorMessage.INTERNAL_SERVER_ERROR;
        final HttpStatus status = ErrorMessage.getHttpStatus(errorMessage);
        final ApiError body = createApiError(errorMessage);
        return ResponseEntity.status(status).body(body);
    }

    private ApiError createApiError(ErrorMessage errorMessage) {
        final String message = ErrorMessage.getMessage(errorMessage);
        return new ApiError(errorMessage, message);
    }

    private ApiError createApiError(ErrorMessage errorMessage, String message) {
        return new ApiError(errorMessage, message);
    }

}
