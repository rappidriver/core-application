package com.rappidrive.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VehicleYear Value Object Tests")
class VehicleYearTest {
    
    private final int currentYear = LocalDate.now().getYear();
    
    @Test
    @DisplayName("Should create vehicle year with valid current year")
    void shouldCreateWithCurrentYear() {
        VehicleYear year = new VehicleYear(currentYear);
        
        assertThat(year.getValue()).isEqualTo(currentYear);
        assertThat(year.getAge()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should create vehicle year with year within 10 years")
    void shouldCreateWithYearWithin10Years() {
        int validYear = currentYear - 5;
        VehicleYear year = new VehicleYear(validYear);
        
        assertThat(year.getValue()).isEqualTo(validYear);
        assertThat(year.getAge()).isEqualTo(5);
    }
    
    @Test
    @DisplayName("Should create vehicle year with exactly 10 years")
    void shouldCreateWithExactly10Years() {
        int validYear = currentYear - 10;
        VehicleYear year = new VehicleYear(validYear);
        
        assertThat(year.getValue()).isEqualTo(validYear);
        assertThat(year.getAge()).isEqualTo(10);
        assertThat(year.isWithinMaxAge()).isTrue();
    }
    
    @Test
    @DisplayName("Should throw exception when year is older than 10 years")
    void shouldThrowExceptionWhenOlderThan10Years() {
        int invalidYear = currentYear - 11;
        
        assertThatThrownBy(() -> new VehicleYear(invalidYear))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be older than 10 years");
    }
    
    @Test
    @DisplayName("Should throw exception when year is before 1900")
    void shouldThrowExceptionWhenBefore1900() {
        assertThatThrownBy(() -> new VehicleYear(1899))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be before 1900");
    }
    
    @Test
    @DisplayName("Should throw exception when year is in the future")
    void shouldThrowExceptionWhenYearInFuture() {
        int futureYear = currentYear + 2;
        
        assertThatThrownBy(() -> new VehicleYear(futureYear))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be in the future");
    }
    
    @Test
    @DisplayName("Should allow next year (for pre-sales)")
    void shouldAllowNextYear() {
        int nextYear = currentYear + 1;
        VehicleYear year = new VehicleYear(nextYear);
        
        assertThat(year.getValue()).isEqualTo(nextYear);
    }
    
    @Test
    @DisplayName("isOlderThan should return true when vehicle is older")
    void isOlderThanShouldReturnTrueWhenOlder() {
        VehicleYear year = new VehicleYear(currentYear - 6);
        
        assertThat(year.isOlderThan(5)).isTrue();
        assertThat(year.isOlderThan(6)).isFalse();
        assertThat(year.isOlderThan(7)).isFalse();
    }
    
    @Test
    @DisplayName("isWithinMaxAge should return false when older than 10 years (boundary)")
    void isWithinMaxAgeShouldWorkCorrectly() {
        VehicleYear year10 = new VehicleYear(currentYear - 10);
        
        assertThat(year10.isWithinMaxAge()).isTrue();
    }
    
    @Test
    @DisplayName("Should be equal when same year")
    void shouldBeEqualWhenSameYear() {
        VehicleYear year1 = new VehicleYear(2020);
        VehicleYear year2 = new VehicleYear(2020);
        
        assertThat(year1).isEqualTo(year2);
        assertThat(year1.hashCode()).isEqualTo(year2.hashCode());
    }
    
    @Test
    @DisplayName("Should not be equal when different years")
    void shouldNotBeEqualWhenDifferentYears() {
        VehicleYear year1 = new VehicleYear(2020);
        VehicleYear year2 = new VehicleYear(2021);
        
        assertThat(year1).isNotEqualTo(year2);
    }
    
    @Test
    @DisplayName("ToString should return year as string")
    void toStringShouldReturnYear() {
        VehicleYear year = new VehicleYear(2020);
        
        assertThat(year.toString()).isEqualTo("2020");
    }
}
