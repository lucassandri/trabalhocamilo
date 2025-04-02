package com.programacao_web.rpg_market.model;

public enum ProductRarity {
    COMUM("Comum", "bg-secondary"),
    INCOMUM("Incomum", "bg-success"),
    RARO("Raro", "bg-primary"),
    EPICO("Épico", "bg-purple"),
    LENDARIO("Lendário", "bg-warning");
    
    private final String displayName;
    private final String badgeClass;
    
    ProductRarity(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getBadgeClass() {
        return badgeClass;
    }
}