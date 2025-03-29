package com.bank.application.auth_app.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RegistraceUzivatelDTO {

    @NotBlank(message = "Křestní jméno je povinné.")
    private String jmeno;

    @NotBlank(message = "Příjmení je povinné.")
    private String prijmeni;

    @Email(message = "E-mail musí být validní.")
    @NotBlank(message = "E-mail je povinný.")
    private String email;

    @Size(min = 8, message = "Heslo musí mít alespoň 8 znaků.")
    private String heslo;

    @NotBlank(message = "Telefonní číslo je povinné.")
    private String telefon;

    @Past(message = "Datum narození musí být v minulosti.")
    private LocalDate datumNarozeni;


}
