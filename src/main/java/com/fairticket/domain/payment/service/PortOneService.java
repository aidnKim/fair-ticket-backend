package com.fairticket.domain.payment.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fairticket.domain.payment.dto.PortOnePaymentResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortOneService {

    private final RestTemplate restTemplate;

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.api-secret}")
    private String apiSecret;

    private static final String PORTONE_API_URL = "https://api.iamport.kr";

    /**
     * 1. 액세스 토큰 발급
     */
    public String getAccessToken() {
        String url = PORTONE_API_URL + "/users/getToken";

        Map<String, String> body = Map.of(
            "imp_key", apiKey,
            "imp_secret", apiSecret
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody().get("response");

        return (String) responseBody.get("access_token");
    }

    /**
     * 2. 결제 정보 조회
     */
    public PortOnePaymentResponseDto getPaymentInfo(String impUid) {
        String accessToken = getAccessToken();
        String url = PORTONE_API_URL + "/payments/" + impUid;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, Map.class
        );

        Map<String, Object> data = (Map<String, Object>) response.getBody().get("response");

        PortOnePaymentResponseDto result = new PortOnePaymentResponseDto(
        	    (String) data.get("imp_uid"),
        	    (String) data.get("merchant_uid"),
        	    new BigDecimal(String.valueOf(data.get("amount"))),
        	    (String) data.get("status")
        	    );

        return result;
    }
    
    /**
     * 3. 결제 취소
     */
    public void cancelPayment(String impUid, String reason) {
        String accessToken = getAccessToken();
        String url = PORTONE_API_URL + "/payments/cancel";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
            "imp_uid", impUid,
            "reason", reason
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, entity, Map.class);
    }
}