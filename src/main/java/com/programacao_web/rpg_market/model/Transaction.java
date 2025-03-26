package com.programacao_web.rpg_market.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private User buyer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;
    
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime completedAt;
    
    private String trackingCode; // Código de rastreio para envios

    // Se a Transaction tiver outros nomes para estes campos, use-os:
    public void setCreateDate(LocalDateTime createDate) {
        this.createdAt = createDate;
    }

    public void setFinalizedDate(LocalDateTime finalizedDate) {
        this.completedAt = finalizedDate;
    }
}
