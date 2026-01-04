package com.example.monoauction.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Getter
public enum ErrorMessage {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"User Not Found With These Details"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,"Email Already In Use Plz Try Again"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,"Invalid Credentials"),
    INSUFFICIENT_BALANCE(HttpStatus.PAYMENT_REQUIRED,"Insufficient Balance"),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED,"Unauthorized Access"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something Wrong Happened"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request");

    private final HttpStatus httpStatus;
    private final String description;

    private ErrorMessage(HttpStatus httpStatus,String description){
        this.httpStatus = httpStatus;
        this.description = description;
    }

    public static HttpStatus getHttpStatus(ErrorMessage errorMessage) {
        return Arrays.stream(ErrorMessage.values())
                .filter(element -> element.equals(errorMessage))
                .findFirst()
                .map(element -> element.getHttpStatus())
                .orElse(null);

    }

    public static String getMessage(ErrorMessage errorMessage) {
        return Arrays.stream(ErrorMessage.values())
                .filter(element -> element.equals(errorMessage))
                .findFirst()
                .map(ErrorMessage::getDescription)
                .orElse(null);
    }

    public static ErrorMessage getFromHttpStatus(HttpStatus code) {
        return Arrays.stream(ErrorMessage.values())
                .filter(element -> element.getHttpStatus().equals(code))
                .findFirst()
                .orElse(INTERNAL_SERVER_ERROR);
    }

}
