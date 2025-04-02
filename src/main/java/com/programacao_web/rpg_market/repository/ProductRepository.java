package com.programacao_web.rpg_market.repository;

import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.ProductCategory;
import com.programacao_web.rpg_market.model.ProductStatus;
import com.programacao_web.rpg_market.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByCategoryAndStatus(ProductCategory category, ProductStatus status, Pageable pageable);
    
    @Query("{'$or': [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}], 'status': ?1}")
    Page<Product> searchByNameAndStatus(String keyword, ProductStatus status, Pageable pageable);
    
    List<Product> findByStatusAndAuctionEndDateLessThanEqual(ProductStatus status, LocalDateTime dateTime);
    List<Product> findBySeller(User seller);
    List<Product> findBySellerAndStatusIn(User seller, List<ProductStatus> statuses);
    
}
