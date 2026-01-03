package com.example.monoauction.execptions;

import com.example.monoauction.model.enums.ErrorMessage;
import lombok.Getter;

@Getter
public class AuctionHouseException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public AuctionHouseException(ErrorMessage errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }
}
