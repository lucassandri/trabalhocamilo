package com.programacao_web.rpg_market.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Document(collection = "products")
public class Product {
    
    @Id
    private String id;
    
    @Field("name")
    private String name;
    
    @Field("description")
    private String description;
    
    @Field("price")
    private BigDecimal price;
    
    @Field("category")
    private ProductCategory category;
    
    @Field("status")
    private ProductStatus status = ProductStatus.AVAILABLE;
    
    @Field("type")
    private ProductType type = ProductType.DIRECT_SALE;
    
    @Field("image_url")
    private String imageUrl;
    
    @DBRef
    private User seller;
    
    @DBRef
    private List<Bid> bids = new ArrayList<>();
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("auction_end_date")
    private LocalDateTime auctionEndDate;
    
    @Field("buy_now_price")
    private BigDecimal buyNowPrice;
    
    @Field("min_bid_increment")
    private BigDecimal minBidIncrement;
    
    // Novos campos para elementos RPG
    @Field("rarity")
    private ItemRarity rarity = ItemRarity.COMUM;
    
    @Field("level_required")
    private Integer levelRequired = 1;
    
    @Field("magic_properties")
    private Set<MagicProperty> magicProperties = new HashSet<>();
    
    @Field("quantity")
    private Integer quantity = 1;
    
    @Field("weight")
    private BigDecimal weight = BigDecimal.ONE;
    
    @Field("durability")
    private Integer durability = 100;
    
    @Field("history")
    private String history;
    
    // Helper method to add magic properties
    public void addMagicProperty(MagicProperty property) {
        if (this.magicProperties == null) {
            this.magicProperties = new HashSet<>();
        }
        this.magicProperties.add(property);
    }

    public ProductCategory getCategory() {
        return category;
    }
    public void setCategory(ProductCategory category) {
        this.category = category;
    }
}
