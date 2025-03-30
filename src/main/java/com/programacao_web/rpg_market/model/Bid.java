package com.programacao_web.rpg_market.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "bids")
public class Bid {
    
    @Id
    private String id;
    
    @DBRef
    private Product product;
    
    @DBRef
    private User bidder;
    
    @Field("amount")
    private BigDecimal amount;
    
    @Field("bid_time")
    private LocalDateTime bidTime = LocalDateTime.now();
    
    @Field("winning")
    private boolean winning;
}
