package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.Rating;
import com.rappidrive.domain.enums.RatingStatus;
import com.rappidrive.domain.enums.RatingType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port para repositório de avaliações.
 */
public interface RatingRepositoryPort {
    
    /**
     * Salva uma avaliação.
     */
    Rating save(Rating rating);
    
    /**
     * Busca avaliação por ID.
     */
    Optional<Rating> findById(UUID id);
    
    /**
     * Busca avaliação específica por viagem, avaliador e tipo.
     */
    Optional<Rating> findByTripIdAndRaterIdAndType(UUID tripId, UUID raterId, RatingType type);
    
    /**
     * Busca todas as avaliações de um usuário (motorista ou passageiro).
     */
    List<Rating> findByRateeIdAndType(UUID rateeId, RatingType type, RatingStatus status);
    
    /**
     * Busca todas as avaliações de uma viagem.
     */
    List<Rating> findByTripId(UUID tripId);
    
    /**
     * Calcula a média de ratings de um usuário.
     */
    double calculateAverageByRateeId(UUID rateeId, RatingType type, RatingStatus status);
    
    /**
     * Conta total de ratings de um usuário.
     */
    long countByRateeIdAndType(UUID rateeId, RatingType type, RatingStatus status);
    
    /**
     * Verifica se já existe avaliação para a combinação viagem/avaliador/tipo.
     */
    boolean existsByTripIdAndRaterIdAndType(UUID tripId, UUID raterId, RatingType type);
    
    /**
     * Busca as avaliações mais recentes de um usuário.
     */
    List<Rating> findRecentByRateeId(UUID rateeId, RatingType type, int limit);
}
