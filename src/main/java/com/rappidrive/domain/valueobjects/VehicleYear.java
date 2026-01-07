package com.rappidrive.domain.valueobjects;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object representando o ano de fabricação de um veículo.
 * Regra de negócio: Veículos não podem ter mais de 10 anos de uso.
 */
public final class VehicleYear {
    
    private static final int MAX_AGE = 10;
    private static final int MIN_YEAR = 1900;
    
    private final int value;
    
    /**
     * Cria um ano de veículo.
     * 
     * @param year ano de fabricação
     * @throws IllegalArgumentException se o ano for inválido ou muito antigo
     */
    public VehicleYear(int year) {
        validateYear(year);
        this.value = year;
    }
    
    private void validateYear(int year) {
        int currentYear = LocalDate.now().getYear();
        
        if (year < MIN_YEAR) {
            throw new IllegalArgumentException(
                String.format("Vehicle year cannot be before %d. Got: %d", MIN_YEAR, year)
            );
        }
        
        if (year > (currentYear + 1)) {
            throw new IllegalArgumentException(
                String.format("Vehicle year cannot be in the future. Current year: %d, got: %d", 
                    currentYear, year)
            );
        }
        
        if (year < (currentYear - MAX_AGE)) {
            throw new IllegalArgumentException(
                String.format("Vehicle cannot be older than %d years. Current year: %d, got: %d", 
                    MAX_AGE, currentYear, year)
            );
        }
    }
    
    /**
     * Retorna o ano de fabricação.
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Calcula a idade do veículo em anos.
     */
    public int getAge() {
        return LocalDate.now().getYear() - value;
    }
    
    /**
     * Verifica se o veículo é mais antigo que N anos.
     */
    public boolean isOlderThan(int years) {
        return getAge() > years;
    }
    
    /**
     * Verifica se o veículo atende ao requisito de idade máxima (10 anos).
     */
    public boolean isWithinMaxAge() {
        return getAge() <= MAX_AGE;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleYear that = (VehicleYear) o;
        return value == that.value;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
