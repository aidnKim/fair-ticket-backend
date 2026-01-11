package com.fairticket.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PaymentCreateRequestDto {
    private Long reservationId;
    String impUid;		// 포트원에서 받은 거래 고유번호
    String merchantUid;	// 프론트에서 생성해서 포트원에 넘긴 주문번호
}