package com.bank.application.auth_app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String jmeno;
    private String prijmeni;
    private String email;
}
