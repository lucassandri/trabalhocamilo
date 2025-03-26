package com.programacao_web.rpg_market.model;

public enum TransactionStatus {
    PENDING, // Pagamento pendente
    PAID, // Pago, aguardando envio
    SHIPPED, // Enviado
    DELIVERED, // Entregue
    COMPLETED, // Concluído com sucesso
    DISPUTED, // Em disputa
    CANCELED, // Cancelado
    REFUNDED // Reembolsado
}
