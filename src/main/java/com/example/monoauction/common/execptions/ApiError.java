package com.example.monoauction.common.execptions;

import com.example.monoauction.authentication.model.enums.ErrorMessage;

public record ApiError(ErrorMessage code, String message) {
}
