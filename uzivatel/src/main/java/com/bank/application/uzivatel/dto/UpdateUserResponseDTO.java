package com.bank.application.uzivatel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUserResponseDTO {

    private String qrCodeBase64;
    private String message;
}
