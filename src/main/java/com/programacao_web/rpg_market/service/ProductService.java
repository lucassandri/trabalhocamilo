package com.programacao_web.rpg_market.service;

import com.programacao_web.rpg_market.model.*;
import com.programacao_web.rpg_market.repository.BidRepository;
import com.programacao_web.rpg_market.repository.ProductRepository;
import com.programacao_web.rpg_market.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private BidRepository bidRepository;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    /**
     * Cria um novo produto
     */
    public Product create(Product product, User seller) {
        product.setSeller(seller);
        
        // Se for leilão, verifica se tem data de término
        if (product.getType() == ProductType.AUCTION && product.getAuctionEndDate() == null) {
            // Define um padrão de 7 dias se não for especificado
            product.setAuctionEndDate(LocalDateTime.now().plusDays(7));
            product.setStatus(ProductStatus.AUCTION_ACTIVE);
        }
        
        return productRepository.save(product);
    }
    
    /**
     * Busca um produto pelo ID
     */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    /**
     * Busca produtos disponíveis para venda direta
     */
    public Page<Product> findAvailable(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.AVAILABLE, pageable);
    }
    
    /**
     * Busca leilões ativos
     */
    public Page<Product> findActiveAuctions(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.AUCTION_ACTIVE, pageable);
    }
    
    /**
     * Busca produtos por categoria
     */
    public Page<Product> findByCategory(ProductCategory category, Pageable pageable) {
        return productRepository.findByCategoryAndStatus(category, ProductStatus.AVAILABLE, pageable);
    }
    
    /**
     * Busca produtos por palavra-chave
     */
    public Page<Product> search(String keyword, ProductStatus status, Pageable pageable) {
        return productRepository.searchByNameAndStatus(keyword, status, pageable);
    }
    
    /**
     * Busca os lances de um produto
     */
    public List<Bid> getProductBids(Product product) {
        return bidRepository.findByProductOrderByAmountDesc(product);
    }
    
    /**
     * Realiza um lance em um leilão
     */
    @Transactional
    public void makeBid(Product product, User bidder, BigDecimal amount) {
        if (product.getType() != ProductType.AUCTION || 
            product.getStatus() != ProductStatus.AUCTION_ACTIVE) {
            throw new IllegalArgumentException("Este produto não está em leilão ativo");
        }
        
        // Verifica se o usuário é o vendedor
        if (bidder.getId().equals(product.getSeller().getId())) {
            throw new IllegalArgumentException("O vendedor não pode dar lances em seu próprio item");
        }
        
        // Verifica se o lance é maior que o lance atual
        Optional<Bid> highestBid = bidRepository.findHighestBidForProduct(product);
        if (highestBid.isPresent() && highestBid.get().getAmount().compareTo(amount) >= 0) {
            throw new IllegalArgumentException("O lance deve ser maior que o lance atual");
        }
        
        // Verifica o incremento mínimo
        if (highestBid.isPresent() && product.getMinBidIncrement() != null) {
            BigDecimal minAmount = highestBid.get().getAmount().add(product.getMinBidIncrement());
            if (amount.compareTo(minAmount) < 0) {
                throw new IllegalArgumentException("O lance deve ser pelo menos " + 
                    product.getMinBidIncrement() + " maior que o lance atual");
            }
        }
        
        // Cria o novo lance
        Bid bid = new Bid();
        bid.setProduct(product);
        bid.setBidder(bidder);
        bid.setAmount(amount);
        bidRepository.save(bid);
        
        // Atualiza o preço atual do produto
        product.setPrice(amount);
        productRepository.save(product);
        
        // Atualiza o lance vencedor
        highestBid.ifPresent(b -> {
            b.setWinning(false);
            bidRepository.save(b);
        });
        bid.setWinning(true);
        bidRepository.save(bid);
        
        // Verifica se é uma compra imediata (buy now)
        if (product.getBuyNowPrice() != null && 
            amount.compareTo(product.getBuyNowPrice()) >= 0) {
            endAuction(product);
        }
    }
    
    /**
     * Realiza uma compra direta
     */
    @Transactional
    public void buyNow(Product product, User buyer) {
        // Verifica se o comprador não é o vendedor
        if (buyer.getId().equals(product.getSeller().getId())) {
            throw new IllegalArgumentException("Você não pode comprar seu próprio item");
        }
        
        if (product.getStatus() != ProductStatus.AVAILABLE && 
            product.getStatus() != ProductStatus.AUCTION_ACTIVE) {
            throw new IllegalArgumentException("Este produto não está disponível para compra");
        }
        
        BigDecimal price = product.getType() == ProductType.AUCTION ? 
            product.getBuyNowPrice() : product.getPrice();
            
        if (price == null) {
            throw new IllegalArgumentException("Preço não definido para este produto");
        }
        
        product.setStatus(ProductStatus.SOLD);
        productRepository.save(product);
        
        // Cria uma transação
        transactionService.createTransaction(product, buyer, product.getSeller(), price);
    }
    
    /**
     * Verifica periodicamente leilões que terminaram
     */
    @Scheduled(fixedRate = 60000) // Executa a cada minuto
    @Transactional
    public void checkEndedAuctions() {
        List<Product> endedAuctions = productRepository.findByStatusAndAuctionEndDateLessThanEqual(
            ProductStatus.AUCTION_ACTIVE, LocalDateTime.now());
            
        for (Product auction : endedAuctions) {
            endAuction(auction);
        }
    }
    
    /**
     * Finaliza um leilão
     */
    private void endAuction(Product product) {
        product.setStatus(ProductStatus.AUCTION_ENDED);
        productRepository.save(product);
        
        // Processa o vencedor
        Optional<Bid> winningBid = bidRepository.findHighestBidForProduct(product);
        if (winningBid.isPresent()) {
            // Cria uma transação para o vencedor
            transactionService.createTransaction(
                product, 
                winningBid.get().getBidder(), 
                product.getSeller(), 
                winningBid.get().getAmount()
            );
        }
    }

    /**
     * Atualiza um produto existente
     */
    @Transactional
    public Product update(Long id, Product updatedProduct, User seller) {
        Optional<Product> existingProductOpt = productRepository.findById(id);
        if (existingProductOpt.isEmpty()) {
            throw new IllegalArgumentException("Produto não encontrado");
        }
        
        Product existingProduct = existingProductOpt.get();
        
        // Verifica se o produto pertence ao vendedor
        if (!existingProduct.getSeller().getId().equals(seller.getId())) {
            throw new IllegalArgumentException("Você não tem permissão para editar este produto");
        }
        
        // Atualiza apenas os campos permitidos
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setCategory(updatedProduct.getCategory());
        
        // Se for um leilão, permite atualizar o preço de compra imediata e incremento mínimo
        if (existingProduct.getType() == ProductType.AUCTION && 
            existingProduct.getStatus() != ProductStatus.AUCTION_ENDED) {
            existingProduct.setBuyNowPrice(updatedProduct.getBuyNowPrice());
            existingProduct.setMinBidIncrement(updatedProduct.getMinBidIncrement());
        }
        
        // Se houver nova imagem, atualiza ela
        if (updatedProduct.getImageUrl() != null && !updatedProduct.getImageUrl().isEmpty()) {
            existingProduct.setImageUrl(updatedProduct.getImageUrl());
        }
        
        return productRepository.save(existingProduct);
    }

    /**
     * Retorna ranking de vendedores com mais vendas
     */
    public List<User> getTopSellers() {
        return transactionRepository.findTopSellers();
    }

    /**
     * Retorna ranking de compradores com mais compras
     */
    public List<User> getTopBuyers() {
        return transactionRepository.findTopBuyers();
    }
}
