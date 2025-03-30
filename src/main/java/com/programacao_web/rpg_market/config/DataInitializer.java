package com.programacao_web.rpg_market.config;

import com.programacao_web.rpg_market.model.*;
import com.programacao_web.rpg_market.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository userRepository, 
                          ProductRepository productRepository,
                          PasswordEncoder passwordEncoder) {
        return args -> {

            // First, check if we need to initialize data
            if (productRepository.count() == 0) {
                // Create some products
                Product product1 = new Product();
                product1.setName("Espada Longa");
                product1.setDescription("Uma espada longa e afiada.");
                product1.setPrice(new BigDecimal("150.00"));
                product1.setCategory(ProductCategory.ARMAS);
                productRepository.save(product1);

                Product product2 = new Product();
                product2.setName("Poção de Vida");
                product2.setDescription("Restaura 50 pontos de vida.");
                product2.setPrice(new BigDecimal("50.00"));
                product2.setCategory(ProductCategory.POCOES_ELIXIRES);
                productRepository.save(product2);

                System.out.println("MongoDB initialized with sample products");
            }
            if (userRepository.count() == 0) {
                // Create admin user
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@rpgmarket.com");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setCharacterClass("Mago");
                admin.setLevel(99);
                admin.setExperience(1000);
                admin.setGoldCoins(new BigDecimal("10000"));
                admin.setRole(UserRole.ROLE_MESTRE);
                userRepository.save(admin);
                
                System.out.println("MongoDB initialized with admin user");

            }
        };
    }
}