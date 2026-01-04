package com.example.monoauction.common.dto;

import com.example.monoauction.common.enums.ErrorMessage;

public record ErrorResponse(ErrorMessage code, String message) {
}
