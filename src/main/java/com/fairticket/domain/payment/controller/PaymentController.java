package com.fairticket.domain.payment.controller;

import com.fairticket.domain.payment.dto.PaymentCreateRequestDto;
import com.fairticket.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 처리
    @PostMapping
    public ResponseEntity<String> processPayment(Principal principal,
                                                 @RequestBody PaymentCreateRequestDto requestDto) {
        Long paymentId = paymentService.processPayment(principal.getName(), requestDto);
        return ResponseEntity.ok("결제가 완료되었습니다! ID: " + paymentId);
    }
}