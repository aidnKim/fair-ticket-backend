package com.fairticket.domain.queue.scheduler;

import com.fairticket.domain.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueScheduler {
	
    private final QueueService queueService;
    
    @Scheduled(fixedRate = 5000)  // 5초마다
    public void processQueue() {
        Long scheduleId = 1L;  // 테스트용
        if (queueService.getQueueSize(scheduleId) > 0) {
            queueService.allowNextUsers(scheduleId, 10);
        }
    }
}
