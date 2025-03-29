package com.bank.application.auth_app.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "uzivatel", schema = "uzivatel")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String jmeno;
    private String prijmeni;
    private String email;
    private String heslo;

}
