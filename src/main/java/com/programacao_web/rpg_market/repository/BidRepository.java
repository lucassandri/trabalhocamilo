package com.programacao_web.rpg_market.repository;

import com.programacao_web.rpg_market.model.Bid;
import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByProductOrderByAmountDesc(Product product);
    List<Bid> findByBidder(User bidder);
    
    @Query("SELECT b FROM Bid b WHERE b.product = :product AND b.amount = (SELECT MAX(b2.amount) FROM Bid b2 WHERE b2.product = :product)")
    Optional<Bid> findHighestBidForProduct(Product product);
}
