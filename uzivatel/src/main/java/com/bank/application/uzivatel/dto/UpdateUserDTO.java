package com.bank.application.uzivatel.dto;

import lombok.Data;

@Data
public class UpdateUserDTO {
    private String jmeno;
    private String prijmeni;
    private String email;
    private String adresa;
}

