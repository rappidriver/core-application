package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.domain.enums.VehicleStatus;
import com.rappidrive.infrastructure.persistence.entities.VehicleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository para veículos.
 */
@Repository
public interface SpringDataVehicleRepository extends JpaRepository<VehicleJpaEntity, UUID> {
    
    /**
     * Busca veículos por tenant.
     */
    List<VehicleJpaEntity> findByTenantId(UUID tenantId);
    
    /**
     * Busca veículos por motorista.
     */
    List<VehicleJpaEntity> findByDriverId(UUID driverId);
    
    /**
     * Busca veículo ativo de um motorista.
     */
    @Query("SELECT v FROM VehicleJpaEntity v WHERE v.driverId = :driverId AND v.status = 'ACTIVE'")
    Optional<VehicleJpaEntity> findActiveByDriverId(@Param("driverId") UUID driverId);
    
    /**
     * Busca veículo por placa dentro de um tenant.
     */
    Optional<VehicleJpaEntity> findByLicensePlateAndTenantId(String licensePlate, UUID tenantId);
    
    /**
     * Verifica se existe veículo com a placa no tenant.
     */
    boolean existsByLicensePlateAndTenantId(String licensePlate, UUID tenantId);
}
