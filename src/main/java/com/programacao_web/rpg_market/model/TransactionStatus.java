package com.programacao_web.rpg_market.model;

public enum TransactionStatus {
    PENDING("Pendente"),
    PROCESSING("Processando"),
    SHIPPED("Enviado"),
    DELIVERED("Entregue"),  // Added missing status
    COMPLETED("Concluído"),
    CANCELED("Cancelado"),
    DISPUTED("Em Disputa"),
    REFUNDED("Reembolsado");  // Added missing status
    
    private final String displayName;
    
    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
