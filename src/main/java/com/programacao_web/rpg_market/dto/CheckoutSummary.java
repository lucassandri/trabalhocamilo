package com.programacao_web.rpg_market.dto;

import com.programacao_web.rpg_market.model.DeliveryAddress;
import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.User;
import java.math.BigDecimal;

public class CheckoutSummary {
    
    private Product product;
    private User buyer;
    private User seller;
    private BigDecimal totalAmount;
    private BigDecimal goldBalance;
    private Boolean hasSufficientFunds;
    private DeliveryAddress deliveryAddress;
    private String notes;
    
    // Informações específicas do tipo de compra
    private String purchaseType; // "DIRECT_SALE" ou "AUCTION_BID" ou "AUCTION_BUY_NOW"
    private BigDecimal bidAmount; // Para lances em leilão
    private BigDecimal currentBid; // Lance atual (para leilões)
    private BigDecimal minBidAmount; // Lance mínimo necessário
    
    // Taxas e custos adicionais (para futuras expansões)
    private BigDecimal shippingCost = BigDecimal.ZERO;
    private BigDecimal serviceFee = BigDecimal.ZERO;
    
    public BigDecimal getGrandTotal() {
        return totalAmount != null ? totalAmount.add(shippingCost).add(serviceFee) : BigDecimal.ZERO;
    }
    
    // Getters and setters
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public User getBuyer() {
        return buyer;
    }
    
    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }
    
    public User getSeller() {
        return seller;
    }
    
    public void setSeller(User seller) {
        this.seller = seller;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getGoldBalance() {
        return goldBalance;
    }
    
    public void setGoldBalance(BigDecimal goldBalance) {
        this.goldBalance = goldBalance;
    }
    
    public Boolean getHasSufficientFunds() {
        return hasSufficientFunds;
    }
    
    public void setHasSufficientFunds(Boolean hasSufficientFunds) {
        this.hasSufficientFunds = hasSufficientFunds;
    }
    
    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(DeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPurchaseType() {
        return purchaseType;
    }
    
    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }
    
    public BigDecimal getBidAmount() {
        return bidAmount;
    }
    
    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }
    
    public BigDecimal getCurrentBid() {
        return currentBid;
    }
    
    public void setCurrentBid(BigDecimal currentBid) {
        this.currentBid = currentBid;
    }
    
    public BigDecimal getMinBidAmount() {
        return minBidAmount;
    }
    
    public void setMinBidAmount(BigDecimal minBidAmount) {
        this.minBidAmount = minBidAmount;
    }
    
    public BigDecimal getShippingCost() {
        return shippingCost;
    }
    
    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }
    
    public BigDecimal getServiceFee() {
        return serviceFee;
    }
    
    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }
}
