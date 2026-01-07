package com.rappidrive.domain.valueobjects;

/**
 * Enumeration of supported currencies.
 */
public enum Currency {
    BRL("Brazilian Real", "R$"),
    USD("US Dollar", "$"),
    EUR("Euro", "€");
    
    private final String displayName;
    private final String symbol;
    
    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getSymbol() {
        return symbol;
    }
}
