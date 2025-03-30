package com.programacao_web.rpg_market.repository;

import com.programacao_web.rpg_market.model.Bid;
import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends MongoRepository<Bid, String> {
    List<Bid> findByProductOrderByAmountDesc(Product product);
    List<Bid> findByBidder(User bidder);
    
    @Query("{ 'product' : ?0, 'amount' : { $eq: ?1 } }")
    Optional<Bid> findByProductAndAmount(Product product, String highestAmount);
    
    @Query(value="{ 'product' : ?0 }", sort="{ 'amount' : -1 }")
    List<Bid> findByProductSortedByAmountDesc(Product product);
    
    default Optional<Bid> findHighestBidForProduct(Product product) {
        List<Bid> bids = findByProductSortedByAmountDesc(product);
        return bids.isEmpty() ? Optional.empty() : Optional.of(bids.get(0));
    }
}
