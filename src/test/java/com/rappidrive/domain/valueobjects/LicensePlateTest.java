package com.rappidrive.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LicensePlate Value Object Tests")
class LicensePlateTest {
    
    @Test
    @DisplayName("Should create license plate with old format (ABC-1234)")
    void shouldCreateWithOldFormat() {
        LicensePlate plate = new LicensePlate("ABC-1234");
        
        assertThat(plate.getValue()).isEqualTo("ABC1234");
        assertThat(plate.getFormatted()).isEqualTo("ABC-1234");
        assertThat(plate.isMercosulFormat()).isFalse();
    }
    
    @Test
    @DisplayName("Should create license plate with old format without hyphen")
    void shouldCreateWithOldFormatWithoutHyphen() {
        LicensePlate plate = new LicensePlate("ABC1234");
        
        assertThat(plate.getValue()).isEqualTo("ABC1234");
        assertThat(plate.getFormatted()).isEqualTo("ABC-1234");
    }
    
    @Test
    @DisplayName("Should create license plate with Mercosul format (ABC1D23)")
    void shouldCreateWithMercosulFormat() {
        LicensePlate plate = new LicensePlate("ABC1D23");
        
        assertThat(plate.getValue()).isEqualTo("ABC1D23");
        assertThat(plate.getFormatted()).isEqualTo("ABC1D23");
        assertThat(plate.isMercosulFormat()).isTrue();
    }
    
    @Test
    @DisplayName("Should normalize license plate to uppercase")
    void shouldNormalizeToUppercase() {
        LicensePlate plate = new LicensePlate("abc-1234");
        
        assertThat(plate.getValue()).isEqualTo("ABC1234");
    }
    
    @Test
    @DisplayName("Should remove hyphens and spaces")
    void shouldRemoveHyphensAndSpaces() {
        LicensePlate plate = new LicensePlate("ABC - 1234");
        
        assertThat(plate.getValue()).isEqualTo("ABC1234");
    }
    
    @Test
    @DisplayName("Should throw exception for null value")
    void shouldThrowExceptionForNullValue() {
        assertThatThrownBy(() -> new LicensePlate(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should throw exception for empty value")
    void shouldThrowExceptionForEmptyValue() {
        assertThatThrownBy(() -> new LicensePlate(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should throw exception for blank value")
    void shouldThrowExceptionForBlankValue() {
        assertThatThrownBy(() -> new LicensePlate("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should throw exception for invalid format - too short")
    void shouldThrowExceptionForTooShort() {
        assertThatThrownBy(() -> new LicensePlate("ABC123"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid license plate format");
    }
    
    @Test
    @DisplayName("Should throw exception for invalid format - too long")
    void shouldThrowExceptionForTooLong() {
        assertThatThrownBy(() -> new LicensePlate("ABC12345"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid license plate format");
    }
    
    @Test
    @DisplayName("Should throw exception for invalid format - wrong pattern")
    void shouldThrowExceptionForWrongPattern() {
        assertThatThrownBy(() -> new LicensePlate("1234ABC"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid license plate format");
    }
    
    @Test
    @DisplayName("Should throw exception for invalid Mercosul format")
    void shouldThrowExceptionForInvalidMercosul() {
        assertThatThrownBy(() -> new LicensePlate("ABC1234D"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid license plate format");
    }
    
    @Test
    @DisplayName("Should be equal when same plate value")
    void shouldBeEqualWhenSamePlate() {
        LicensePlate plate1 = new LicensePlate("ABC-1234");
        LicensePlate plate2 = new LicensePlate("ABC1234");
        
        assertThat(plate1).isEqualTo(plate2);
        assertThat(plate1.hashCode()).isEqualTo(plate2.hashCode());
    }
    
    @Test
    @DisplayName("Should not be equal when different plates")
    void shouldNotBeEqualWhenDifferentPlates() {
        LicensePlate plate1 = new LicensePlate("ABC-1234");
        LicensePlate plate2 = new LicensePlate("DEF-5678");
        
        assertThat(plate1).isNotEqualTo(plate2);
    }
    
    @Test
    @DisplayName("ToString should return formatted plate")
    void toStringShouldReturnFormattedPlate() {
        LicensePlate plate = new LicensePlate("ABC1234");
        
        assertThat(plate.toString()).isEqualTo("ABC-1234");
    }
}
