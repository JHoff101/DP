package com.bank.application.uzivatel.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transakce", schema = "uzivatel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transakce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "uzivatel_id", nullable = false)
    private User uzivatel;

    @Column(nullable = false)
    private BigDecimal castka;

    @Column(nullable = false)
    private LocalDateTime datum;

    @Column(nullable = false)
    private String typ;
}

