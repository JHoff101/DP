package com.bank.application.uzivatel.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponseDTO {
    private String jmeno;
    private String prijmeni;
    private String email;
    private String adresa;
    private int telefonniCislo;
}
