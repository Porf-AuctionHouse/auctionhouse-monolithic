package com.example.monoauction.common.execptions;

import com.example.monoauction.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Objects>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Objects>> handleBusinessException(
            BusinessException ex, WebRequest request){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Objects>> handleException(
            Exception ex, WebRequest request){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Objects>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex){
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
    }

//    @ExceptionHandler(AuctionHouseException.class)
//    public ResponseEntity<ErrorResponse> handleCrmException(AuctionHouseException exception) {
//        final ErrorMessage errorMessage = exception.getErrorMessage();
//        final HttpStatus status = ErrorMessage.getHttpStatus(errorMessage);
//        final ErrorResponse body = createErrorResponse(errorMessage);
//        return ResponseEntity.status(status).body(body);
//    }
//
//    @ExceptionHandler(value = {
//            MethodArgumentTypeMismatchException.class,
//    })
//
//    public ResponseEntity<ErrorResponse> handleExceptions(MethodArgumentTypeMismatchException exception) {
//        final ErrorMessage errorMessage = ErrorMessage.BAD_REQUEST;
//        final HttpStatus status = ErrorMessage.getHttpStatus(errorMessage);
//
//        final String paramName = exception.getName();
//
//        String requiredTypeName = "unknown";
//        if (exception.getRequiredType() != null) {
//            requiredTypeName = exception.getRequiredType().getSimpleName();
//        }
//
//        String providedTypeName = "unknown";
//        if (exception.getValue() != null) {
//            providedTypeName = exception.getValue().getClass().getSimpleName();
//        }
//
//        final String message = String.format(
//                "Invalid value for parameter '%s'. Expected type: '%s', but got: '%s'.",
//                paramName, requiredTypeName, providedTypeName
//        );
//
//        final ErrorResponse body = createErrorResponse(errorMessage, message);
//        return ResponseEntity.status(status).body(body);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
//        final Map<String, String> errors = new HashMap<>();
//
//        exception.getBindingResult()
//                .getFieldErrors()
//                .forEach((error) -> {
//                    final String fieldName = error.getField();
//                    final String errorMessage = error.getDefaultMessage();
//                    errors.put(fieldName, errorMessage);
//                });
//
//        final ErrorMessage errorMessage = ErrorMessage.BAD_REQUEST;
//        final HttpStatus status = ErrorMessage.getHttpStatus(errorMessage);
//        final ErrorResponse body = new ErrorResponse(errorMessage, errors.toString());
//        return new ResponseEntity<>(body, status);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
//        final ErrorMessage errorMessage = ErrorMessage.INTERNAL_SERVER_ERROR;
//        final HttpStatus status = ErrorMessage.getHttpStatus(errorMessage);
//        final ErrorResponse body = createErrorResponse(errorMessage);
//        return ResponseEntity.status(status).body(body);
//    }
//
//    private ErrorResponse createErrorResponse(ErrorMessage errorMessage) {
//        final String message = ErrorMessage.getMessage(errorMessage);
//        return new ErrorResponse(errorMessage, message);
//    }
//
//    private ErrorResponse createErrorResponse(ErrorMessage errorMessage, String message) {
//        return new ErrorResponse(errorMessage, message);
//    }

}
