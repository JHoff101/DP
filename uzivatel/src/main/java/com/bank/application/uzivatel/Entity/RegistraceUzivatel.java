package com.bank.application.uzivatel.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "uzivatel", schema = "auth")
public class RegistraceUzivatel implements Serializable {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @Column(nullable = false)
        private String jmeno;

        @Column(nullable = false)
        private String prijmeni;

        @Column(unique = true, nullable = false)
        private String email;

        @Column(nullable = false)
        private String heslo;

        @Column(nullable = false)
        private String telefon;

        @Column(name = "datum_narozeni", nullable = false)
        private LocalDate datumNarozeni;

        @ManyToOne
        @JoinColumn(name = "role_id", nullable = false)
        private Role role;

        @OneToOne
        @JoinColumn(name = "user_id", referencedColumnName = "id")
        private User user;


}
