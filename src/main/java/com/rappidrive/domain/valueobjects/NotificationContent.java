package com.rappidrive.domain.valueobjects;

import com.rappidrive.domain.exceptions.InvalidNotificationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Value Object representando o conteúdo de uma notificação.
 * Encapsula título, mensagem e dados adicionais (deeplinks, IDs de entidades).
 */
public final class NotificationContent {
    
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_MESSAGE_LENGTH = 500;
    
    private final String title;
    private final String message;
    private final Map<String, String> data;
    
    private NotificationContent(String title, String message, Map<String, String> data) {
        this.title = title;
        this.message = message;
        this.data = data != null ? Collections.unmodifiableMap(new HashMap<>(data)) : Collections.emptyMap();
    }
    
    /**
     * Cria conteúdo de notificação com validações
     */
    public static NotificationContent of(String title, String message) {
        return of(title, message, null);
    }
    
    /**
     * Cria conteúdo de notificação com dados adicionais
     */
    public static NotificationContent of(String title, String message, Map<String, String> data) {
        validateTitle(title);
        validateMessage(message);
        return new NotificationContent(title, message, data);
    }
    
    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidNotificationException("Título da notificação não pode ser vazio");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new InvalidNotificationException(
                "Título da notificação não pode exceder " + MAX_TITLE_LENGTH + " caracteres"
            );
        }
    }
    
    private static void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new InvalidNotificationException("Mensagem da notificação não pode ser vazia");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidNotificationException(
                "Mensagem da notificação não pode exceder " + MAX_MESSAGE_LENGTH + " caracteres"
            );
        }
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Map<String, String> getData() {
        return data;
    }
    
    public boolean hasData() {
        return !data.isEmpty();
    }
    
    public String getDataValue(String key) {
        return data.get(key);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationContent that = (NotificationContent) o;
        return Objects.equals(title, that.title) 
            && Objects.equals(message, that.message) 
            && Objects.equals(data, that.data);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, message, data);
    }
    
    @Override
    public String toString() {
        return "NotificationContent{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
