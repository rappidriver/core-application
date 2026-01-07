package com.rappidrive.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a monetary amount with currency.
 * Immutable and uses BigDecimal for precision.
 */
public final class Money {
    
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    private final BigDecimal amount;
    private final Currency currency;
    
    /**
     * Creates a new Money instance.
     * 
     * @param amount the monetary amount
     * @param currency the currency
     * @throws IllegalArgumentException if amount or currency is null, or amount is negative
     */
    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
        this.currency = currency;
    }
    
    /**
     * Creates a new Money instance with BRL currency (convenience constructor).
     * 
     * @param amount the monetary amount
     * @throws IllegalArgumentException if amount is null or negative
     */
    public Money(BigDecimal amount) {
        this(amount, Currency.BRL);
    }
    
    /**
     * Creates a new Money instance from a double value with BRL currency.
     * 
     * @param amount the monetary amount
     * @return a new Money instance
     */
    public Money(double amount) {
        this(BigDecimal.valueOf(amount), Currency.BRL);
    }
    
    /**
     * Creates a new Money instance from a double value.
     * 
     * @param amount the monetary amount
     * @param currency the currency
     * @return a new Money instance
     */
    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }
    
    /**
     * Creates a new Money instance with zero amount.
     * 
     * @param currency the currency
     * @return a new Money instance with zero amount
     */
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
    
    /**
     * Adds another Money amount to this one.
     * 
     * @param other the other Money to add
     * @return a new Money instance with the sum
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Subtracts another Money amount from this one.
     * 
     * @param other the other Money to subtract
     * @return a new Money instance with the difference
     * @throws IllegalArgumentException if currencies don't match or result is negative
     */
    public Money subtract(Money other) {
        ensureSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtraction would result in negative amount");
        }
        return new Money(result, this.currency);
    }
    
    /**
     * Multiplies this Money by a factor.
     * 
     * @param factor the multiplication factor
     * @return a new Money instance with the product
     * @throws IllegalArgumentException if factor is negative
     */
    public Money multiply(double factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Factor cannot be negative");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }
    
    /**
     * Multiplies this Money by a BigDecimal factor.
     * 
     * @param factor the multiplication factor
     * @return a new Money instance with the product
     * @throws IllegalArgumentException if factor is negative
     */
    public Money multiply(BigDecimal factor) {
        if (factor == null) {
            throw new IllegalArgumentException("Factor cannot be null");
        }
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Factor cannot be negative");
        }
        return new Money(this.amount.multiply(factor), this.currency);
    }
    
    /**
     * Checks if this Money is greater than another.
     * 
     * @param other the other Money to compare
     * @return true if this is greater than other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isGreaterThan(Money other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Checks if this Money is less than another.
     * 
     * @param other the other Money to compare
     * @return true if this is less than other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isLessThan(Money other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }
    
    /**
     * Checks if this Money amount is zero.
     * 
     * @return true if amount is zero
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Checks if this Money amount is negative.
     * 
     * @return true if amount is negative
     */
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * Checks if this Money amount is negative or zero.
     * 
     * @return true if amount is negative or zero
     */
    public boolean isNegativeOrZero() {
        return this.amount.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    /**
     * Checks if this Money amount is positive.
     * 
     * @return true if amount is positive
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    private void ensureSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Cannot operate on different currencies: %s and %s",
                    this.currency, other.currency)
            );
        }
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 && currency == money.currency;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", currency.getSymbol(), amount);
    }
}
