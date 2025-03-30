package com.programacao_web.rpg_market.model;

public enum ItemRarity {
    COMUM("Comum", "badge-secondary"),
    INCOMUM("Incomum", "badge-success"),
    RARO("Raro", "badge-primary"),
    MUITO_RARO("Muito Raro", "badge-warning"),
    LENDARIO("Lendário", "badge-danger"),
    ARTEFATO("Artefato", "badge-gold");
    
    private final String displayName;
    private final String badgeClass;
    
    ItemRarity(String displayName, String badgeClass) {
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