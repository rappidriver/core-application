package com.rappidrive.infrastructure.adapters;

import com.rappidrive.application.ports.output.NotificationServicePort;
import com.rappidrive.domain.entities.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stub adapter para NotificationServicePort.
 * Implementação futura: integração com Firebase Cloud Messaging (FCM).
 * Atualmente apenas loga as notificações que seriam enviadas.
 */
@Component
public class StubNotificationServiceAdapter implements NotificationServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(StubNotificationServiceAdapter.class);
    
    @Override
    public boolean sendPushNotification(Notification notification) {
        // TODO: Implementar integração com FCM
        logger.info("STUB: Push notification would be sent - Type: {}, Priority: {}, User: {}, Title: '{}'",
            notification.getType(),
            notification.getPriority(),
            notification.getUserId().getValue(),
            notification.getContent().getTitle()
        );
        
        // Simular sucesso
        return true;
    }
    
    @Override
    public boolean isAvailable() {
        // Stub sempre disponível (não envia de verdade)
        return true;
    }
}
