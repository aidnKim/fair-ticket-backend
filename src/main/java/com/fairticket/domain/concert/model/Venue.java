package com.fairticket.domain.concert.model;

import java.util.Arrays;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "venue_id")
    private Long id;

    @Column(nullable = false)
    private String name;  // "올림픽공원 체조경기장"

    @Column(nullable = false)
    private int totalRows;  // 5 (A~E)

    @Column(nullable = false)
    private int seatsPerRow;  // 10

    private String vipRows;  // "A,B" (쉼표 구분)

    @Builder
    public Venue(String name, int totalRows, int seatsPerRow, String vipRows) {
        this.name = name;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
        this.vipRows = vipRows;
    }

    public boolean isVipRow(String row) {
        if (vipRows == null) return false;
        return Arrays.asList(vipRows.split(",")).contains(row);
    }
}