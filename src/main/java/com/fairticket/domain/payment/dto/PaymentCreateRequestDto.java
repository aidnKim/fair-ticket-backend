package com.fairticket.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PaymentCreateRequestDto {
    private Long reservationId;
    private BigDecimal amount;
}