package com.fairticket.domain.queue.controller;

import com.fairticket.domain.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class QueueController {
	
    private final QueueService queueService;
    
    @PostMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> enterQueue(
            @PathVariable Long scheduleId, Principal principal) {
        Long position = queueService.enterQueue(scheduleId, principal.getName());
        return ResponseEntity.ok(Map.of(
            "position", position,
            "message", position + "번째로 대기 중입니다."
        ));
    }
    
    @GetMapping("/{scheduleId}/position")
    public ResponseEntity<Map<String, Object>> getPosition(
            @PathVariable Long scheduleId, Principal principal) {
        Long position = queueService.getQueuePosition(scheduleId, principal.getName());
        boolean canEnter = queueService.canEnter(scheduleId, principal.getName());
        return ResponseEntity.ok(Map.of(
            "position", position,
            "canEnter", canEnter
        ));
    }
}
