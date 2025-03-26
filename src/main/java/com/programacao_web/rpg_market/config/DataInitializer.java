package com.programacao_web.rpg_market.config;

import com.programacao_web.rpg_market.model.*;
import com.programacao_web.rpg_market.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private BidRepository bidRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Bean
    CommandLineRunner initData() {
        return args -> {
            // Verifica se já existem dados
            if (userRepository.count() > 0) {
                return;  // Não inicializa dados novamente
            }
            
            System.out.println("Inicializando dados de teste...");
            
            // Criar usuários
            User admin = new User();
            admin.setUsername("mestre");
            admin.setPassword(passwordEncoder.encode("senha123"));
            admin.setEmail("mestre@rpgmarket.com");
            admin.setCharacterClass("Mestre");
            admin.setLevel(30);
            admin.setExperience(2950);
            admin.setGoldCoins(5000);
            admin.setRole(UserRole.MESTRE);
            
            User user1 = new User();
            user1.setUsername("gandalf");
            user1.setPassword(passwordEncoder.encode("senha123"));
            user1.setEmail("gandalf@rpgmarket.com");
            user1.setCharacterClass("Mago");
            user1.setLevel(10);
            user1.setExperience(920);
            user1.setGoldCoins(300);
            
            User user2 = new User();
            user2.setUsername("aragorn");
            user2.setPassword(passwordEncoder.encode("senha123"));
            user2.setEmail("aragorn@rpgmarket.com");
            user2.setCharacterClass("Guerreiro");
            user2.setLevel(8);
            user2.setExperience(750);
            user2.setGoldCoins(450);
            
            User user3 = new User();
            user3.setUsername("legolas");
            user3.setPassword(passwordEncoder.encode("senha123"));
            user3.setEmail("legolas@rpgmarket.com");
            user3.setCharacterClass("Ranger");
            user3.setLevel(7);
            user3.setExperience(650);
            user3.setGoldCoins(250);
            
            // Salva usuários
            List<User> users = Arrays.asList(admin, user1, user2, user3);
            userRepository.saveAll(users);
            
            // Criar produtos para venda direta
            Product sword = new Product();
            sword.setName("Espada Élfica Encantada");
            sword.setDescription("Uma espada antiga forjada pelos elfos, capaz de brilhar na presença de inimigos.");
            sword.setPrice(new BigDecimal("150.00"));
            sword.setCategory(ProductCategory.ARMAS);
            sword.setType(ProductType.DIRECT_SALE);
            sword.setSeller(user1);
            sword.setImageUrl("espada_elfica.jpg");
            
            Product potion = new Product();
            potion.setName("Poção de Cura Suprema");
            potion.setDescription("Restaura 100 pontos de vida instantaneamente. Efeito imediato.");
            potion.setPrice(new BigDecimal("75.50"));
            potion.setCategory(ProductCategory.POCOES_ELIXIRES);
            potion.setType(ProductType.DIRECT_SALE);
            potion.setSeller(user2);
            potion.setImageUrl("pocao_cura.jpg");
            
            Product armor = new Product();
            armor.setName("Armadura de Couro Reforçada");
            armor.setDescription("Armadura leve que oferece boa proteção sem sacrificar a mobilidade.");
            armor.setPrice(new BigDecimal("120.00"));
            armor.setCategory(ProductCategory.ARMADURA_VESTIMENTA);
            armor.setType(ProductType.DIRECT_SALE);
            armor.setSeller(user3);
            armor.setImageUrl("armadura_couro.jpg");
            
            // Criar produtos para leilão
            Product magicStaff = new Product();
            magicStaff.setName("Cajado do Arquimago");
            magicStaff.setDescription("Um poderoso cajado que pertenceu ao lendário arquimago Tormek.");
            magicStaff.setPrice(new BigDecimal("200.00"));  // Lance inicial
            magicStaff.setCategory(ProductCategory.ARMAS);
            magicStaff.setType(ProductType.AUCTION);
            magicStaff.setStatus(ProductStatus.AUCTION_ACTIVE);
            magicStaff.setSeller(user1);
            magicStaff.setBuyNowPrice(new BigDecimal("500.00"));
            magicStaff.setMinBidIncrement(new BigDecimal("10.00"));
            magicStaff.setAuctionEndDate(LocalDateTime.now().plusDays(5));
            magicStaff.setImageUrl("cajado_arquimago.jpg");
            
            Product amulet = new Product();
            amulet.setName("Amuleto de Proteção");
            amulet.setDescription("Concede imunidade a efeitos negativos de magia. Artefato raro.");
            amulet.setPrice(new BigDecimal("150.00"));  // Lance inicial
            amulet.setCategory(ProductCategory.JOIAS_ARTEFATOS);
            amulet.setType(ProductType.AUCTION);
            amulet.setStatus(ProductStatus.AUCTION_ACTIVE);
            amulet.setSeller(user2);
            amulet.setBuyNowPrice(new BigDecimal("350.00"));
            amulet.setMinBidIncrement(new BigDecimal("5.00"));
            amulet.setAuctionEndDate(LocalDateTime.now().plusDays(3));
            amulet.setImageUrl("amuleto_protecao.jpg");
            
            // Salvar produtos
            List<Product> products = Arrays.asList(sword, potion, armor, magicStaff, amulet);
            productRepository.saveAll(products);
            
            // Corrigir os lances para o leilão do cajado
            Bid bid1 = new Bid();
            bid1.setProduct(magicStaff);
            bid1.setBidder(user2);
            bid1.setAmount(new BigDecimal("210.00"));
            bid1.setBidTime(LocalDateTime.now().minusHours(12));
            bid1.setWinning(false);
            
            Bid bid2 = new Bid();
            bid2.setProduct(magicStaff);
            bid2.setBidder(user3);
            bid2.setAmount(new BigDecimal("230.00"));
            bid2.setBidTime(LocalDateTime.now().minusHours(8));
            bid2.setWinning(true);
            
            // Salvar lances
            bidRepository.saveAll(Arrays.asList(bid1, bid2));
            
            // Atualizar o preço do produto para o lance atual
            magicStaff.setPrice(new BigDecimal("230.00"));
            productRepository.save(magicStaff);
            
            // Criar uma transação completa (produto vendido)
            Product soldItem = new Product();
            soldItem.setName("Grimório Arcano");
            soldItem.setDescription("Um livro antigo com feitiços raros e conhecimentos arcanos perdidos.");
            soldItem.setPrice(new BigDecimal("300.00"));
            soldItem.setCategory(ProductCategory.PERGAMINHOS_LIVROS);
            soldItem.setType(ProductType.DIRECT_SALE);
            soldItem.setStatus(ProductStatus.SOLD);
            soldItem.setSeller(user1);
            soldItem.setImageUrl("grimorio_arcano.jpg");
            productRepository.save(soldItem);
            
            Transaction transaction = new Transaction();
            transaction.setProduct(soldItem);
            transaction.setBuyer(user3);
            transaction.setSeller(user1);
            transaction.setAmount(new BigDecimal("300.00"));
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCreatedAt(LocalDateTime.now().minusDays(2));
            transaction.setCompletedAt(LocalDateTime.now().minusDays(1));
            transactionRepository.save(transaction);
            
            System.out.println("Dados de teste inicializados com sucesso!");
        };
    }
}