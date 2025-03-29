package com.bank.application.admin_app.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "role", schema = "auth")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nazev;

    @Column(nullable = false)
    private String popis;
}

