package com.fairticket.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.fairticket.domain.concert.model.Concert;
import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fairticket.domain.concert.model.Seat;
import com.fairticket.domain.concert.model.SeatGrade;
import com.fairticket.domain.concert.model.SeatStatus;
import com.fairticket.domain.concert.repository.ConcertRepository;
import com.fairticket.domain.concert.repository.ConcertScheduleRepository;
import com.fairticket.domain.concert.repository.SeatRepository;
import com.fairticket.domain.reservation.dto.ReservationCreateRequestDto;
import com.fairticket.domain.reservation.model.Reservation;
import com.fairticket.domain.reservation.model.ReservationStatus;
import com.fairticket.domain.reservation.repository.ReservationRepository;
import com.fairticket.domain.user.model.User;
import com.fairticket.domain.user.model.UserRole;
import com.fairticket.domain.user.repository.UserRepository;

@SpringBootTest // 통합 테스트 (스프링 컨테이너 로드)
class ReservationServiceTest {

    @Autowired private ReservationService reservationService;
    @Autowired private UserRepository userRepository;
    @Autowired private ConcertRepository concertRepository;
    @Autowired private ConcertScheduleRepository scheduleRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private ReservationRepository reservationRepository;

    private Long scheduleId;
    private Long seatId;

    // 테스트 실행 전마다 데이터 세팅
    @BeforeEach
    void setUp() {
        // 1. 유저 100명 생성 (동시 접속자 흉내)
        // (실제 DB에 저장하지 않고, 이름만 다르게 해서 요청 보낼 때 사용할 예정)
        // 여기서는 대표로 1명만 미리 저장해두거나, 테스트 루프 안에서 생성해도 됨.
        // 편의상 루프 안에서 즉석으로 유저를 만들어서 저장하도록 로직 구성 예정.
        
        // 2. 공연 및 스케줄 생성
        Concert concert = Concert.builder()
                .title("테스트 콘서트")
                .description("설명")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        concertRepository.save(concert);

        ConcertSchedule schedule = ConcertSchedule.builder()
                .concert(concert)
                .concertDate(LocalDateTime.now().plusHours(5))
                .totalSeats(1) // 좌석 1개
                .build();
        scheduleRepository.save(schedule);
        this.scheduleId = schedule.getId();

        // 3. 좌석 생성 (1개)
        Seat seat = Seat.builder()
                .schedule(schedule)
                .seatNo(1)
                .grade(SeatGrade.VIP)
                .price(BigDecimal.valueOf(100000))
                .status(SeatStatus.AVAILABLE)
                .build();
        seatRepository.save(seat);
        this.seatId = seat.getId();
    }

    @Test
    @DisplayName("좌석 1개에 100명이 동시에 예약을 시도하면, 오직 1명만 성공해야 한다.")
    void concurrency_test() throws InterruptedException {
        // given
        int threadCount = 100;
        // 32개의 스레드 풀 생성 (동시 요청을 처리할 일꾼들)
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        // 100개의 요청이 다 끝날 때까지 기다리게 하는 장치
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 성공 횟수, 실패 횟수 카운트
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            int userIndex = i + 1;
            executorService.submit(() -> {
                try {
                    // 1. 각 요청마다 새로운 유저 생성 및 저장 (트랜잭션 분리 필요하지만 간단히 처리)
                    // 주의: 실제로는 유저 생성을 미리 해두는 게 좋지만, 코드를 줄이기 위해 여기서 처리
                    // 동시성 테스트에서 유저 생성 부분에서 충돌 날 수 있으니
                    // 유니크한 이메일로 생성
                    String email = "user" + userIndex + "@test.com";
                    saveUser(email); // 아래 헬퍼 메소드 사용

                    // 2. 예약 요청 DTO 생성
                    ReservationCreateRequestDto requestDto = new ReservationCreateRequestDto();
                    // DTO에 Setter가 없으므로 ReflectionTestUtils로 값 주입
                    ReflectionTestUtils.setField(requestDto, "scheduleId", scheduleId);
                    ReflectionTestUtils.setField(requestDto, "seatId", seatId);

                    // 3. 예약 서비스 호출! (핵심)
                    reservationService.createReservation(email, requestDto);
                    
                    // 성공하면 카운트 증가
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    // 실패하면(이미 예약된 좌석 등) 카운트 증가
                    // System.out.println("예약 실패: " + e.getMessage()); // 로그 확인용
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown(); // 작업 하나 끝남을 알림
                }
            });
        }

        latch.await(); // 100명이 다 끝날 때까지 여기서 대기

        // then (검증)
        // 1. 예약은 딱 1건만 생겨야 함
        long totalReservations = reservationRepository.count();
        assertThat(totalReservations).isEqualTo(1);

        // 2. 성공 카운트도 1이어야 함
        assertThat(successCount.get()).isEqualTo(1);
        
        // 3. 실패 카운트는 99여야 함
        assertThat(failCount.get()).isEqualTo(99);

        // 4. 좌석 상태는 '일시 예약(TEMPORARY_RESERVED)' 상태여야 함
        Seat seat = seatRepository.findById(seatId).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.TEMPORARY_RESERVED);
    }
    
    // 유저 저장 헬퍼 메소드 (동시성 이슈 방지를 위해 별도 처리보다는 간단하게 구현)
    // 테스트 환경에서는 repository.save()가 thread-safe 하게 동작하길 기대
    private void saveUser(String email) {
        // 락 충돌 방지를 위해 try-catch로 감싸거나 미리 생성하는 게 정석이지만,
        // 여기서는 간단하게 진행. 만약 유저 생성에서 에러나면 미리 생성 방식으로 바꿔야 함.
        try {
            userRepository.save(User.builder()
                    .email(email)
                    .password("1234")
                    .name("tester")
                    .role(UserRole.USER)
                    .build());
        } catch (Exception e) {
            // 이미 존재하면 패스
        }
    }
    
    @Test
    @DisplayName("결제되지 않은 예약이 만료 시간(5분)을 넘기면 자동으로 취소되어야 한다.")
    void scheduler_test() {
        // given
        // 1. 과거의 예약 생성 (이미 10분 전에 예약한 상황 연출)
        // 1-1. 유저 생성
        User user = userRepository.save(User.builder()
                .email("lazy_user@test.com")
                .password("1234")
                .name("lazy")
                .role(UserRole.USER)
                .build());

        // 1-2. 좌석 상태를 '예약 중'으로 설정
        Seat seat = seatRepository.findById(seatId).orElseThrow();
        seat.reserve();
        seatRepository.saveAndFlush(seat);

        // 1-3. 10분 전 시간으로 예약 생성 (직접 Repository로 저장해서 시간 조작)
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10); // 10분 전
        
        Reservation reservation = Reservation.builder()
                .user(user)
                .seat(seat)
                .schedule(seat.getSchedule())
                .reservationTime(pastTime) // 10분 전에 예약함 -> 만료시간(5분 뒤)은 이미 5분 전임
                .build();
        
        // 엔티티 로직에 따라 expireTime이 자동으로 세팅된다고 가정
        // 만약 expireTime을 빌더에서 안 넣고 내부에서 계산한다면 reservationTime만 과거로 주면 됨
        // 혹시 몰라 expireTime 필드도 빌더에 있다면 아래처럼 명시 (없으면 위 코드로 충분)
        // .expireTime(pastTime.plusMinutes(5)) 
        
        reservationRepository.save(reservation);

        // when
        // 스케줄러가 호출하는 메소드를 강제로 실행!
        reservationService.cancelExpiredReservations();

        // then
        // 1. 예약 상태가 CANCELLED로 바뀌었는지 확인
        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);

        // 2. 좌석 상태가 다시 AVAILABLE(구매 가능)로 풀렸는지 확인
        Seat updatedSeat = seatRepository.findById(seatId).orElseThrow();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }
}