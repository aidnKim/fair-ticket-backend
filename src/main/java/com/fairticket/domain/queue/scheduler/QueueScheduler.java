package com.fairticket.domain.queue.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fairticket.domain.concert.repository.ConcertScheduleRepository;
import com.fairticket.domain.queue.service.QueueService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueueScheduler {
	
    private final QueueService queueService;
    private final ConcertScheduleRepository scheduleRepository;
    
    @Scheduled(fixedRate = 5000)  // 5초마다
    public void processQueue() {
    	// 활성화된 모든 스케줄 조회
        List<ConcertSchedule> activeSchedules = scheduleRepository.findAll();
        
        for (ConcertSchedule schedule : activeSchedules) {
            Long scheduleId = schedule.getId();
            if (queueService.getQueueSize(scheduleId) > 0) {
                queueService.allowNextUsers(scheduleId, 10);
            }
        }
    }
}
