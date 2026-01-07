package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.Notification;

/**
 * Port de saída para serviço externo de notificações push (FCM).
 * Implementação futura - atualmente stub para estrutura.
 */
public interface NotificationServicePort {
    
    /**
     * Envia push notification via Firebase Cloud Messaging
     * 
     * @param notification Notificação a ser enviada
     * @return true se enviada com sucesso, false caso contrário
     */
    boolean sendPushNotification(Notification notification);
    
    /**
     * Verifica se serviço de push está disponível
     */
    boolean isAvailable();
}
