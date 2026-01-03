package com.example.monoauction.execptions;

import com.example.monoauction.model.enums.ErrorMessage;

public record ApiError(ErrorMessage code, String message) {
}
