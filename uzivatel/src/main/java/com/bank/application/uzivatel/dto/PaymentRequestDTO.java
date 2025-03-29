package com.bank.application.uzivatel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentRequestDTO {

    private Long recipientId;
    private BigDecimal castka;
}
