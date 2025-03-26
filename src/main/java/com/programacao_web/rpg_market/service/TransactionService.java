package com.programacao_web.rpg_market.service;

import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.Transaction;
import com.programacao_web.rpg_market.model.TransactionStatus;
import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Cria uma nova transação para uma compra ou lance vencedor
     */
    @Transactional
    public Transaction createTransaction(Product product, User buyer, User seller, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setProduct(product);
        transaction.setBuyer(buyer);
        transaction.setSeller(seller);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        
        transaction = transactionRepository.save(transaction);
        
        // Dá experiência para comprador e vendedor
        userService.addExperience(buyer, 10);
        userService.addExperience(seller, 15);
        
        return transaction;
    }
    
    /**
     * Atualiza o status de uma transação (versão simplificada)
     */
    @Transactional
    public Transaction updateStatus(Long id, TransactionStatus newStatus) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transação não encontrada");
        }
        
        Transaction transaction = transactionOpt.get();
        transaction.setStatus(newStatus);
        
        if (newStatus == TransactionStatus.COMPLETED) {
            transaction.setCompletedAt(LocalDateTime.now());
            
            // Experiência extra ao completar
            userService.addExperience(transaction.getBuyer(), 5);
            userService.addExperience(transaction.getSeller(), 5);
        }
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Busca transações de um comprador
     */
    public List<Transaction> getBuyerTransactions(User buyer) {
        return transactionRepository.findByBuyer(buyer);
    }
    
    /**
     * Busca transações de um vendedor
     */
    public List<Transaction> getSellerTransactions(User seller) {
        return transactionRepository.findBySeller(seller);
    }
    
    /**
     * Busca uma transação por ID
     */
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }
    
    /**
     * Atualiza o status de uma transação, com verificação de permissões
     */
    @Transactional
    public Transaction updateStatus(Long id, TransactionStatus newStatus, User requestingUser) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transação não encontrada");
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Verifica se o usuário é parte da transação
        if (!transaction.getBuyer().getId().equals(requestingUser.getId()) && 
            !transaction.getSeller().getId().equals(requestingUser.getId())) {
            throw new IllegalArgumentException("Você não tem permissão para atualizar esta transação");
        }
        
        // Verifica se a mudança de status é válida (implementação simplificada)
        if (newStatus == TransactionStatus.SHIPPED && 
            !transaction.getSeller().getId().equals(requestingUser.getId())) {
            throw new IllegalArgumentException("Apenas o vendedor pode marcar como enviado");
        }
        
        if (newStatus == TransactionStatus.DELIVERED && 
            !transaction.getBuyer().getId().equals(requestingUser.getId())) {
            throw new IllegalArgumentException("Apenas o comprador pode confirmar o recebimento");
        }
        
        // Atualiza o status
        transaction.setStatus(newStatus);
        
        if (newStatus == TransactionStatus.COMPLETED) {
            transaction.setCompletedAt(LocalDateTime.now());
            
            // Experiência extra ao completar
            userService.addExperience(transaction.getBuyer(), 5);
            userService.addExperience(transaction.getSeller(), 5);
        }
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Adiciona código de rastreio (para vendedor)
     */
    @Transactional
    public Transaction addTrackingCode(Long id, String trackingCode, User seller) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transação não encontrada");
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Verifica se o usuário é o vendedor
        if (!transaction.getSeller().getId().equals(seller.getId())) {
            throw new IllegalArgumentException("Apenas o vendedor pode adicionar código de rastreio");
        }
        
        transaction.setTrackingCode(trackingCode);
        transaction.setStatus(TransactionStatus.SHIPPED);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Confirma recebimento (para comprador)
     */
    @Transactional
    public Transaction confirmReceipt(Long id, User buyer) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transação não encontrada");
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Verifica se o usuário é o comprador
        if (!transaction.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Apenas o comprador pode confirmar o recebimento");
        }
        
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        
        // Adiciona experiência para comprador e vendedor
        userService.addExperience(transaction.getBuyer(), 10);
        userService.addExperience(transaction.getSeller(), 15);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Abre uma disputa (para comprador)
     */
    @Transactional
    public Transaction openDispute(Long id, String reason, User buyer) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transação não encontrada");
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Verifica se o usuário é o comprador
        if (!transaction.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Apenas o comprador pode abrir uma disputa");
        }
        
        // Verifica se a transação está em status que permite abrir disputa
        if (transaction.getStatus() == TransactionStatus.COMPLETED || 
            transaction.getStatus() == TransactionStatus.DISPUTED ||
            transaction.getStatus() == TransactionStatus.CANCELED ||
            transaction.getStatus() == TransactionStatus.REFUNDED) {
            throw new IllegalArgumentException("Não é possível abrir disputa para esta transação no estado atual");
        }
        
        transaction.setStatus(TransactionStatus.DISPUTED);
        // Idealmente armazenaria o motivo da disputa em um campo adicional ou tabela separada
        
        return transactionRepository.save(transaction);
    }
}
