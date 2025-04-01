package com.programacao_web.rpg_market.model;

public enum ProductStatus {
    AVAILABLE, // Disponível para compra
    RESERVED, // Reservado (em processo de compra)
    SOLD, // Vendido
    AUCTION_ACTIVE, // Leilão ativo
    AUCTION_ENDED, // Leilão finalizado
    CANCELED // Leilão ou produto cancelado
}
