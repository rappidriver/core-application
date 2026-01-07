package com.rappidrive.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithBigDecimal() {
        Money money = new Money(BigDecimal.valueOf(100.50), Currency.BRL);
        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals(Currency.BRL, money.getCurrency());
    }

    @Test
    void shouldCreateMoneyWithDoubleUsingOf() {
        Money money = Money.of(100.50, Currency.USD);
        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals(Currency.USD, money.getCurrency());
    }

    @Test
    void shouldCreateZeroMoney() {
        Money money = Money.zero(Currency.EUR);
        assertEquals(BigDecimal.ZERO.setScale(2), money.getAmount());
        assertEquals(Currency.EUR, money.getCurrency());
    }

    @Test
    void shouldRoundToTwoDecimalPlaces() {
        Money money = new Money(BigDecimal.valueOf(100.567), Currency.BRL);
        assertEquals(new BigDecimal("100.57"), money.getAmount());
    }

    @Test
    void shouldAddMoneyOfSameCurrency() {
        Money money1 = Money.of(100.00, Currency.BRL);
        Money money2 = Money.of(50.50, Currency.BRL);
        Money result = money1.add(money2);
        
        assertEquals(new BigDecimal("150.50"), result.getAmount());
        assertEquals(Currency.BRL, result.getCurrency());
    }

    @Test
    void shouldSubtractMoneyOfSameCurrency() {
        Money money1 = Money.of(100.00, Currency.BRL);
        Money money2 = Money.of(30.50, Currency.BRL);
        Money result = money1.subtract(money2);
        
        assertEquals(new BigDecimal("69.50"), result.getAmount());
        assertEquals(Currency.BRL, result.getCurrency());
    }

    @Test
    void shouldMultiplyByDouble() {
        Money money = Money.of(100.00, Currency.BRL);
        Money result = money.multiply(1.5);
        
        assertEquals(new BigDecimal("150.00"), result.getAmount());
    }

    @Test
    void shouldMultiplyByBigDecimal() {
        Money money = Money.of(100.00, Currency.BRL);
        Money result = money.multiply(BigDecimal.valueOf(2.5));
        
        assertEquals(new BigDecimal("250.00"), result.getAmount());
    }

    @Test
    void shouldCompareGreaterThan() {
        Money money1 = Money.of(100.00, Currency.BRL);
        Money money2 = Money.of(50.00, Currency.BRL);
        
        assertTrue(money1.isGreaterThan(money2));
        assertFalse(money2.isGreaterThan(money1));
    }

    @Test
    void shouldCompareLessThan() {
        Money money1 = Money.of(50.00, Currency.BRL);
        Money money2 = Money.of(100.00, Currency.BRL);
        
        assertTrue(money1.isLessThan(money2));
        assertFalse(money2.isLessThan(money1));
    }

    @Test
    void shouldIdentifyZeroAmount() {
        Money money = Money.zero(Currency.BRL);
        assertTrue(money.isZero());
        
        Money nonZero = Money.of(0.01, Currency.BRL);
        assertFalse(nonZero.isZero());
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Money(null, Currency.BRL));
    }

    @Test
    void shouldRejectNullCurrency() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Money(BigDecimal.valueOf(100), null));
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Money(BigDecimal.valueOf(-10), Currency.BRL));
    }

    @Test
    void shouldRejectAdditionOfDifferentCurrencies() {
        Money brl = Money.of(100.00, Currency.BRL);
        Money usd = Money.of(50.00, Currency.USD);
        
        assertThrows(IllegalArgumentException.class, () -> brl.add(usd));
    }

    @Test
    void shouldRejectSubtractionOfDifferentCurrencies() {
        Money brl = Money.of(100.00, Currency.BRL);
        Money usd = Money.of(50.00, Currency.USD);
        
        assertThrows(IllegalArgumentException.class, () -> brl.subtract(usd));
    }

    @Test
    void shouldRejectSubtractionResultingInNegative() {
        Money money1 = Money.of(50.00, Currency.BRL);
        Money money2 = Money.of(100.00, Currency.BRL);
        
        assertThrows(IllegalArgumentException.class, () -> money1.subtract(money2));
    }

    @Test
    void shouldRejectNegativeMultiplicationFactor() {
        Money money = Money.of(100.00, Currency.BRL);
        
        assertThrows(IllegalArgumentException.class, () -> money.multiply(-1.5));
    }

    @Test
    void shouldRejectNullMultiplicationFactor() {
        Money money = Money.of(100.00, Currency.BRL);
        
        assertThrows(IllegalArgumentException.class, () -> money.multiply((BigDecimal) null));
    }

    @Test
    void shouldRejectComparisonOfDifferentCurrencies() {
        Money brl = Money.of(100.00, Currency.BRL);
        Money usd = Money.of(50.00, Currency.USD);
        
        assertThrows(IllegalArgumentException.class, () -> brl.isGreaterThan(usd));
        assertThrows(IllegalArgumentException.class, () -> brl.isLessThan(usd));
    }

    @Test
    void shouldHaveEqualityBasedOnAmountAndCurrency() {
        Money money1 = Money.of(100.00, Currency.BRL);
        Money money2 = Money.of(100.00, Currency.BRL);
        Money money3 = Money.of(100.00, Currency.USD);
        Money money4 = Money.of(200.00, Currency.BRL);
        
        assertEquals(money1, money2);
        assertNotEquals(money1, money3); // Different currency
        assertNotEquals(money1, money4); // Different amount
    }

    @Test
    void shouldHaveSameHashCodeForEqualMoney() {
        Money money1 = Money.of(100.00, Currency.BRL);
        Money money2 = Money.of(100.00, Currency.BRL);
        
        assertEquals(money1.hashCode(), money2.hashCode());
    }

    @Test
    void shouldFormatToStringWithSymbol() {
        Money brl = Money.of(100.50, Currency.BRL);
        Money usd = Money.of(200.75, Currency.USD);
        
        assertEquals("R$ 100.50", brl.toString());
        assertEquals("$ 200.75", usd.toString());
    }

    @Test
    void shouldBeImmutable() {
        Money original = Money.of(100.00, Currency.BRL);
        Money added = original.add(Money.of(50.00, Currency.BRL));
        Money multiplied = original.multiply(2.0);
        
        // Original should remain unchanged
        assertEquals(new BigDecimal("100.00"), original.getAmount());
        assertEquals(new BigDecimal("150.00"), added.getAmount());
        assertEquals(new BigDecimal("200.00"), multiplied.getAmount());
    }
}
