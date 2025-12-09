package com.fairticket.domain.concert.dto;

import com.fairticket.domain.concert.model.Seat;
import com.fairticket.domain.concert.model.SeatGrade;
import com.fairticket.domain.concert.model.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class SeatResponseDto {
    private Long seatId;
    private int seatNo;
    private SeatGrade grade;
    private BigDecimal price;
    private SeatStatus status; // 판매됨(SOLD)인지 예약가능(AVAILABLE)인지 중요!

    public static SeatResponseDto from(Seat seat) {
        return SeatResponseDto.builder()
                .seatId(seat.getId())
                .seatNo(seat.getSeatNo())
                .grade(seat.getGrade())
                .price(seat.getPrice())
                .status(seat.getStatus())
                .build();
    }
}