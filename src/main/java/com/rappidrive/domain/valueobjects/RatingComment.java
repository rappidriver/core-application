package com.rappidrive.domain.valueobjects;

import com.rappidrive.domain.exceptions.InvalidRatingException;

/**
 * Value Object representando um comentário de avaliação.
 * Comentário é opcional mas quando presente deve respeitar limites e sanitização.
 */
public record RatingComment(String value) {
    
    private static final int MAX_LENGTH = 500;
    
    public RatingComment {
        if (value != null) {
            String sanitized = sanitize(value);
            if (sanitized.length() > MAX_LENGTH) {
                throw new InvalidRatingException(
                    String.format("Comentário deve ter no máximo %d caracteres, recebido: %d", 
                        MAX_LENGTH, sanitized.length())
                );
            }
            value = sanitized;
        }
    }
    
    /**
     * Cria um RatingComment vazio (sem comentário).
     */
    public static RatingComment empty() {
        return new RatingComment(null);
    }
    
    /**
     * Cria um RatingComment a partir de um texto.
     */
    public static RatingComment of(String text) {
        if (text == null || text.isBlank()) {
            return empty();
        }
        return new RatingComment(text.trim());
    }
    
    /**
     * Verifica se o comentário está presente.
     */
    public boolean isPresent() {
        return value != null && !value.isBlank();
    }
    
    /**
     * Verifica se o comentário está vazio.
     */
    public boolean isEmpty() {
        return !isPresent();
    }
    
    /**
     * Retorna o valor ou string vazia se null.
     */
    public String getValueOrEmpty() {
        return value != null ? value : "";
    }
    
    /**
     * Sanitiza o comentário removendo HTML tags e caracteres perigosos.
     */
    private static String sanitize(String text) {
        if (text == null) {
            return null;
        }
        
        // Remove HTML tags
        String sanitized = text.replaceAll("<[^>]*>", "");
        
        // Remove scripts
        sanitized = sanitized.replaceAll("(?i)<script.*?</script>", "");
        
        // Normaliza espaços
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        return sanitized.trim();
    }
}
