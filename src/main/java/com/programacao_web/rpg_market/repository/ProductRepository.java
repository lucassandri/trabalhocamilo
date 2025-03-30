package com.programacao_web.rpg_market.repository;

import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.ProductCategory;
import com.programacao_web.rpg_market.model.ProductStatus;
import com.programacao_web.rpg_market.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByCategoryAndStatus(ProductCategory category, ProductStatus status, Pageable pageable);
    Page<Product> findBySeller(User seller, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE (p.name LIKE %:keyword% OR p.description LIKE %:keyword%) AND p.status = :status")
    Page<Product> searchByNameAndStatus(@Param("keyword") String keyword, @Param("status") ProductStatus status, Pageable pageable);
    
    List<Product> findByStatusAndAuctionEndDateLessThanEqual(ProductStatus status, LocalDateTime dateTime);
    List<Product> findBySeller(User seller);
    List<Product> findBySellerAndStatusIn(User seller, List<ProductStatus> statuses);
}
