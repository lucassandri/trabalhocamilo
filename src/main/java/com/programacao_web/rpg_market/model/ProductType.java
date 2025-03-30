package com.programacao_web.rpg_market.model;

public enum ProductType {
    DIRECT_SALE("Venda Direta"),
    AUCTION("Leilão");
    
    private final String displayName;
    
    ProductType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
