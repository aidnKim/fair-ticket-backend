package com.fairticket.domain.concert.service;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fairticket.domain.concert.repository.ConcertScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatAvailabilityService {
    
    private final RedissonClient redissonClient;
    private final ConcertScheduleRepository scheduleRepository;
    private static final String SEAT_COUNT_KEY = "seat_count:";    
    
    // 잔여석 조회 (Redis 없으면 DB에서 가져와서 초기화)
    public int getAvailableSeats(Long scheduleId) {
        RAtomicLong counter = redissonClient.getAtomicLong(SEAT_COUNT_KEY + scheduleId);
        
        // Redis에 값이 없으면 DB에서 조회 후 초기화
        if (!counter.isExists()) {
            ConcertSchedule schedule = scheduleRepository.findById(scheduleId)
                    .orElse(null);
            if (schedule != null) {
                counter.set(schedule.getAvailableSeats());
                return schedule.getAvailableSeats();
            }
            return 0;
        }
        
        return (int) counter.get();
    }
    
    // 잔여석 초기화 (스케줄 생성 시)
    public void initializeSeats(Long scheduleId, int totalSeats) {
        RAtomicLong counter = redissonClient.getAtomicLong(SEAT_COUNT_KEY + scheduleId);
        counter.set(totalSeats);
    }
    
    // 잔여석 감소 (예매 시)
    public void decreaseSeats(Long scheduleId) {
        redissonClient.getAtomicLong(SEAT_COUNT_KEY + scheduleId).decrementAndGet();
    }
    
    // 잔여석 증가 (취소 시)
    public void increaseSeats(Long scheduleId) {
        redissonClient.getAtomicLong(SEAT_COUNT_KEY + scheduleId).incrementAndGet();
    }
}