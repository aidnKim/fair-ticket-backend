# **결제 검증 실패 시 서버 측 자동 환불 처리 구현**

## **📊 1. 초기 구현 및 잠재적 문제점 발견**

초기에는 프론트엔드에서 결제 성공 시, 백엔드에 결제 정보를 전달하여 데이터베이스에 기록하는 방식으로 기능을 구현했습니다. 하지만 기능 구현 후 코드 리뷰 및 아키텍처 검토 과정에서 다음과 같은 잠재적 문제점을 발견했습니다.

### **초기 아키텍처: FE 주도 취소 방식**

```

결제 요청 (FE → 포트원)
    ↓
결제 완료 (포트원 → FE)
    ↓
검증 요청 (FE → BE)
    ↓
검증 실패 시 에러 응답 (BE → FE)
    ↓
취소 요청 (FE → BE → 포트원)

```

### **⚠️ 문제 상황**

만약 사용자가 결제를 완료했지만, 백엔드 서버가 DB에 저장하는 과정에서 데이터 불일치(ex. 금액 위변조) 등의 이유로 검증에 실패할 경우, **사용자는 돈을 냈지만 예매는 실패하고, 결제가 자동으로 환불되지 않아 돈만 빠져나가는 심각한 상황**이 발생할 수 있었습니다.

이는 데이터 정합성을 깨뜨리고 사용자에게 치명적인 경험을 줄 수 있는 위험한 시나리오였습니다.

### **문제점 상세 분석**

| **문제 유형** | **상세 내용** |
| --- | --- |
| 네트워크 불안정 | 검증 실패 응답 수신 후 브라우저 종료, 네트워크 끊김 시 취소 요청 누락 |
| 보안 취약점 | FE에서 취소 요청 시 위변조 가능성 존재 |
| 통신 비효율 | FE ↔ BE 2회 왕복 필요 (검증 응답 + 취소 요청) |

### **최악의 시나리오**

```

1. 사용자가 10만원 결제 완료 (포트원 결제 성공)
2. BE 검증 실패 (금액 불일치 등)
3. BE → FE로 에러 응답 전송
4. 이 시점에 사용자 브라우저 종료 또는 네트워크 끊김
5. FE의 취소 요청이 BE로 전송되지 않음
6. 결과: 돈은 빠졌는데 예매는 안 됨, 환불도 안 됨

```

---

## **💡 2. 해결 및 개선 과정**

이 문제를 해결하기 위해, **결제 검증과 취소의 책임을 모두 서버가 갖도록** 아키텍처를 개선했습니다.

### **개선된 아키텍처: BE 주도 취소 방식**

```

결제 요청 (FE → 포트원)
    ↓
결제 완료 (포트원 → FE)
    ↓
검증 요청 (FE → BE)
    ↓
검증 실패 감지 → 즉시 자동 취소 (BE → 포트원)
    ↓
결과 응답 (BE → FE): "검증 실패, 자동 환불 완료"

```

### **구체적인 해결책**

백엔드의

**하나의 트랜잭션처럼 처리**

즉, 검증이 실패하면 어떠한 경우에도 반드시 해당 결제가 즉시 환불되도록 보장하는 로직을 추가했습니다.

### **PaymentService - 검증 실패 시 즉시 취소 구현**

```
java

@Transactional
publicLongprocessPayment(String email,PaymentCreateRequestDto requestDto) {
// ... 유저, 예약 검증 로직 ...

// 포트원 결제 검증
PortOnePaymentResponseDtoportOneData=portOneService.getPaymentInfo(requestDto.getImpUid());

// 검증 1: 결제 상태
if (!"paid".equals(portOneData.getStatus())) {
thrownewIllegalArgumentException("결제가 완료되지 않았습니다.");
    }

// 검증 2: 결제 금액 불일치 → 즉시 취소
if (portOneData.getAmount().compareTo(reservation.getSeat().getPrice())!=0) {
portOneService.cancelPayment(requestDto.getImpUid(),"결제 금액 불일치");
thrownewIllegalArgumentException("결제 금액이 일치하지 않습니다. 자동 환불되었습니다.");
    }

// 검증 3: 주문번호 불일치 → 즉시 취소
if (!requestDto.getMerchantUid().equals(portOneData.getMerchantUid())) {
portOneService.cancelPayment(requestDto.getImpUid(),"주문번호 불일치");
thrownewIllegalArgumentException("주문번호가 일치하지 않습니다. 자동 환불되었습니다.");
    }

// 검증 통과 후 예매 확정 로직...
}

```

### **PortOneService - 결제 취소 메서드**

```
java

/**
 * 포트원 결제 취소 API 호출
 */
publicvoidcancelPayment(String impUid,String reason) {
StringaccessToken=getAccessToken();
Stringurl= PORTONE_API_URL+"/payments/cancel";

HttpHeadersheaders=newHttpHeaders();
headers.setBearerAuth(accessToken);
headers.setContentType(MediaType.APPLICATION_JSON);

Map<String,String>body=Map.of(
"imp_uid", impUid,
"reason", reason
    );

HttpEntity<Map<String,String>>entity=newHttpEntity<>(body, headers);
restTemplate.postForEntity(url, entity,Map.class);
}

```

### **FE(Payment.jsx) - 단순화된 에러 처리**

```
javascript

}catch (error) {
console.error("서버 처리 실패:",error);
// BE에서 이미 취소 처리했으므로 안내만 표시
alert(error.response?.data||"결제 처리 중 문제가 발생했습니다. 자동 환불됩니다.");
}

```

### **Trade-off 비교**

| **항목** | **FE 주도 취소 (기존)** | **BE 주도 취소 (개선)** |
| --- | --- | --- |
| 환불 안정성 | ❌ 네트워크 끊김 시 누락 | ✅ 서버에서 즉시 처리 |
| 보안성 | ❌ FE 위변조 가능 | ✅ 서버 측 처리로 안전 |
| 통신 횟수 | ❌ 2회 왕복 | ✅ 1회 왕복 |
| 구현 복잡도 | ✅ 단순 | ⚠️ 예외 처리 필요 |

---

## **📈 3. 결과 및 배운 점**

이 개선을 통해 불일치하는 결제가 발생해도 사용자에게 즉시 환불이 이루어지게 하여 **데이터 정합성을 확보**하고, 더 안정적이고 신뢰도 높은 결제 시스템을 구축할 수 있었습니다.

### **개선 결과**

| **지표** | **개선 전** | **개선 후** |
| --- | --- | --- |
| 네트워크 끊김 시 환불 | ❌ 누락 가능 | ✅ 보장 |
| 통신 왕복 횟수 | 2회 | 1회 |
| 취소 로직 위치 | FE (위변조 가능) | BE (안전) |
| 사용자 경험 | ❌ 수동 환불 요청 필요 | ✅ 자동 환불 처리 |

### **기술적 통찰**

단순히 기능을 구현하는 것을 넘어, **발생 가능한 모든 예외 상황을 고려**하고 시스템의 안정성을 높이는 아키텍처 설계의 중요성을 깊이 배울 수 있었습니다.

```

FE 주도 취소 (클라이언트 의존)
         ↓
네트워크 불안정, 보안 취약점 인식
         ↓
BE 주도 취소 (서버 집중) 전략 선택
         ↓
검증 실패 감지 즉시 원자적 취소 처리

```

### **핵심 개념**

- **Fail-Safe 설계**: 실패 상황에서도 안전하게 복구되는 구조