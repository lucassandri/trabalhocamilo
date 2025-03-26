package com.programacao_web.rpg_market.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    private ProductCategory category;
    
    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.AVAILABLE;
    
    @Enumerated(EnumType.STRING)
    private ProductType type = ProductType.DIRECT_SALE;
    
    private String imageUrl;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids = new ArrayList<>();
    
    private LocalDateTime auctionEndDate; // Para leilões
    
    private BigDecimal minBidIncrement; // Para leilões
    
    private BigDecimal buyNowPrice; // Preço para compra imediata em leilões
}
