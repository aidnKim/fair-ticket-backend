package com.fairticket.domain.payment.controller;

import com.fairticket.domain.payment.dto.PaymentCreateRequestDto;
import com.fairticket.domain.payment.service.PaymentService;
import com.fairticket.domain.payment.service.PortOneService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PortOneService portOneService;

    // 결제 처리
    @PostMapping
    public ResponseEntity<String> processPayment(Principal principal,
                                                 @RequestBody PaymentCreateRequestDto requestDto) {
        Long paymentId = paymentService.processPayment(principal.getName(), requestDto);
        return ResponseEntity.ok("결제가 완료되었습니다! ID: " + paymentId);
    }
    
    @GetMapping("/prepare") // GET /v1/payments/prepare
    public ResponseEntity<String> preparePayment() {
        // 주문번호만 쏙 만들어서 던져줌
        String merchantUid = paymentService.createOrderNum();
        return ResponseEntity.ok(merchantUid);
    }
    
    // 결제 취소(마이페이지에서 사용자 취소 요청 시)
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<String> cancelPayment(Principal principal, @PathVariable Long paymentId) {
        paymentService.cancelPayment(principal.getName(), paymentId);
        return ResponseEntity.ok("결제가 취소되었습니다.");
    }
    
    // 결제 검증 실패 시 자동 취소용 (impUid로 취소)
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelByImpUid(@RequestBody Map<String, String> request) {
        portOneService.cancelPayment(request.get("impUid"), request.get("reason"));
        return ResponseEntity.ok("결제가 취소되었습니다.");
    }
}