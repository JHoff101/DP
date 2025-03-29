package com.bank.application.admin_app.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "user_balance", schema = "uzivatel")
public class UserBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private BigDecimal zustatek;

}

