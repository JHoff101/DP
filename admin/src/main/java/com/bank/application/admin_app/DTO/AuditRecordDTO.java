package com.bank.application.admin_app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditRecordDTO {

    private Long userId;
    private String jmeno;
    private BigDecimal castka;
    private LocalDateTime datum;
    private String typ;
}
