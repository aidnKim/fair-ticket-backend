package com.fairticket.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PortOnePaymentResponseDto {
    private String impUid;
    private String merchantUid;
    private BigDecimal amount;
    private String status;  // "paid", "cancelled", "failed" 등
}