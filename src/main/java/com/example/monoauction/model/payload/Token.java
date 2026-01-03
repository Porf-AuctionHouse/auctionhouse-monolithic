package com.example.monoauction.model.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Token {
    private String accessToken;
    private String refreshToken;
    private String accessExpirationTime;
    private String refreshExpirationTime;
}
