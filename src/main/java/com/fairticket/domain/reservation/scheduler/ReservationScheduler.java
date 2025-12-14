package com.fairticket.domain.reservation.scheduler;

import com.fairticket.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    // 1분마다 실행 (60000ms = 1분)
    // fixedDelay: 이전 작업이 끝나고 나서 설정된 시간 후에 다시 시작
    @Scheduled(fixedDelay = 60000)
    public void checkExpiredReservations() {
        log.debug("예약 만료 체크 스케줄러 실행");
        
        reservationService.cancelExpiredReservations();
    }
}