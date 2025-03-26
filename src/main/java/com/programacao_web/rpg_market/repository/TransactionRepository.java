package com.programacao_web.rpg_market.repository;

import com.programacao_web.rpg_market.model.Transaction;
import com.programacao_web.rpg_market.model.TransactionStatus;
import com.programacao_web.rpg_market.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Busca transações de um comprador
    List<Transaction> findByBuyer(User buyer);
    
    // Busca transações de um vendedor
    List<Transaction> findBySeller(User seller);
    
    // Busca transações por status
    List<Transaction> findByStatus(TransactionStatus status);
    
    // Busca os top vendedores por número de vendas completadas
    @Query(value = "SELECT t.seller FROM Transaction t WHERE t.status = 'COMPLETED' GROUP BY t.seller ORDER BY COUNT(t) DESC")
    List<User> findTopSellers(Pageable pageable);
    
    // Busca os top compradores por número de compras completadas
    @Query(value = "SELECT t.buyer FROM Transaction t WHERE t.status = 'COMPLETED' GROUP BY t.buyer ORDER BY COUNT(t) DESC")
    List<User> findTopBuyers(Pageable pageable);
    
    // Métodos de conveniência para ranking
    default List<User> findTopSellers() {
        return findTopSellers(PageRequest.of(0, 10));
    }
    
    default List<User> findTopBuyers() {
        return findTopBuyers(PageRequest.of(0, 10));
    }
}
