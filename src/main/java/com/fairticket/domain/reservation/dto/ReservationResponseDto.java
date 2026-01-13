package com.fairticket.domain.reservation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fairticket.domain.reservation.model.Reservation;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationResponseDto {
    private Long reservationId;
    private String reservedAt;      // 예매일
    private String concertTitle;    // 공연 제목
    private String concertDate;     // 공연 날짜
    private String seatGrade;       // 좌석 등급 (VIP, R 등)
    private String seatNum;         // 좌석 번호 (예: "A열 3번")
    private String status;          // PENDING, PAID, CANCELLED
    private BigDecimal price;              // 가격
    private LocalDateTime expireTime;  // 만료 시간
    private Long concertId;			// 콘서트 id

    public static ReservationResponseDto from(Reservation reservation) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

        return ReservationResponseDto.builder()
                .reservationId(reservation.getId())
                .reservedAt(reservation.getReservationTime().format(dateFormatter))
                .concertTitle(reservation.getSchedule().getConcert().getTitle())
                .concertDate(reservation.getSchedule().getConcertDate().format(dateTimeFormatter))
                .seatGrade(reservation.getSeat().getGrade().name())
                .seatNum(reservation.getSeat().getSeatRow() + "열 " + reservation.getSeat().getSeatCol() + "번")
                .status(reservation.getStatus().name())
                .price(reservation.getSeat().getPrice())
                .expireTime(reservation.getExpireTime())
                .concertId(reservation.getSchedule().getConcert().getId())
                .build();
    }
}