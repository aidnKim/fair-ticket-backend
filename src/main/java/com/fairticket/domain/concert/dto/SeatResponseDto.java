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
    private String seatRow;
    private int seatCol;
    private String seatLabel;  // "A열 3번" (화면 표시용)
    private SeatGrade grade;
    private BigDecimal price;
    private SeatStatus status; // 판매됨(SOLD)인지 예약가능(AVAILABLE)인지 중요!

    public static SeatResponseDto from(Seat seat) {
        return SeatResponseDto.builder()
                .seatId(seat.getId())
                .seatRow(seat.getSeatRow())
                .seatCol(seat.getSeatCol())
                .seatLabel(seat.getSeatRow() + "열 " + seat.getSeatCol() + "번")
                .grade(seat.getGrade())
                .price(seat.getPrice())
                .status(seat.getStatus())
                .build();
    }
}