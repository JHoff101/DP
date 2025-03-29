package com.bank.application.uzivatel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransakceDTO(Long id, BigDecimal castka, LocalDateTime datum, String typ) {}

