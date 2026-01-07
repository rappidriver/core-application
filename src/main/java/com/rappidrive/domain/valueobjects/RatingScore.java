package com.rappidrive.domain.valueobjects;

import com.rappidrive.domain.exceptions.InvalidRatingException;

/**
 * Value Object representando a pontuação de uma avaliação (1-5 estrelas).
 */
public record RatingScore(int value) {
    
    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    
    public RatingScore {
        if (value < MIN_SCORE || value > MAX_SCORE) {
            throw new InvalidRatingException(
                String.format("Rating score deve estar entre %d e %d, recebido: %d", 
                    MIN_SCORE, MAX_SCORE, value)
            );
        }
    }
    
    /**
     * Cria um RatingScore a partir de um valor.
     */
    public static RatingScore of(int value) {
        return new RatingScore(value);
    }
    
    /**
     * Verifica se é uma avaliação excelente (5 estrelas).
     */
    public boolean isExcellent() {
        return value == 5;
    }
    
    /**
     * Verifica se é uma avaliação boa (4 ou 5 estrelas).
     */
    public boolean isGood() {
        return value >= 4;
    }
    
    /**
     * Verifica se é uma avaliação ruim (1 ou 2 estrelas).
     */
    public boolean isPoor() {
        return value <= 2;
    }
    
    /**
     * Retorna descrição textual da pontuação.
     */
    public String getDescription() {
        return switch (value) {
            case 5 -> "Excelente";
            case 4 -> "Bom";
            case 3 -> "Regular";
            case 2 -> "Ruim";
            case 1 -> "Péssimo";
            default -> "Desconhecido";
        };
    }
}
