package com.rappidrive.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representando uma placa de veículo brasileira.
 * Suporta dois formatos:
 * - Formato antigo: ABC-1234 (3 letras + 4 números)
 * - Formato Mercosul: ABC1D23 (3 letras + 1 número + 1 letra + 2 números)
 */
public final class LicensePlate {
    
    private static final Pattern OLD_FORMAT = Pattern.compile("^[A-Z]{3}[0-9]{4}$");
    private static final Pattern MERCOSUL_FORMAT = Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");
    
    private final String value;
    
    /**
     * Cria uma placa de veículo.
     * 
     * @param plate placa no formato brasileiro (com ou sem hífen)
     * @throws IllegalArgumentException se a placa for inválida
     */
    public LicensePlate(String plate) {
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }
        
        String normalized = normalize(plate);
        validateFormat(normalized);
        this.value = normalized;
    }
    
    /**
     * Normaliza a placa: remove hífens, espaços e converte para maiúsculas.
     */
    private String normalize(String plate) {
        return plate.replaceAll("[-\\s]", "").toUpperCase().trim();
    }
    
    /**
     * Valida o formato da placa.
     */
    private void validateFormat(String plate) {
        if (!OLD_FORMAT.matcher(plate).matches() && !MERCOSUL_FORMAT.matcher(plate).matches()) {
            throw new IllegalArgumentException(
                "Invalid license plate format. Expected ABC-1234 or ABC1D23, got: " + plate
            );
        }
    }
    
    /**
     * Retorna a placa formatada com hífen (formato antigo) ou sem hífen (Mercosul).
     */
    public String getFormatted() {
        if (OLD_FORMAT.matcher(value).matches()) {
            // ABC1234 -> ABC-1234
            return value.substring(0, 3) + "-" + value.substring(3);
        }
        // Mercosul já não usa hífen
        return value;
    }
    
    /**
     * Retorna o valor normalizado (sem hífen, uppercase).
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Verifica se é formato Mercosul.
     */
    public boolean isMercosulFormat() {
        return MERCOSUL_FORMAT.matcher(value).matches();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LicensePlate that = (LicensePlate) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return getFormatted();
    }
}
