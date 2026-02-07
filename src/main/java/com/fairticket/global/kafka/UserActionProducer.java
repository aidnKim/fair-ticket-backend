package com.fairticket.global.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionProducer {
    
    private static final String TOPIC = "user-actions";
    
    private final KafkaTemplate<String, UserActionEvent> kafkaTemplate;
    
    public void sendUserAction(UserActionEvent event) {
    	kafkaTemplate.send(TOPIC, event.getUserEmail() != null ? event.getUserEmail() : event.getSessionId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send message: {}", ex.getMessage());
                } else {
                    log.debug("User action sent: userId={}, action={}", 
                        event.getUserEmail(), event.getActionType());
                }
            });
    }
}
