package com.programacao_web.rpg_market.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    private String characterClass; // Mago, Guerreiro, etc.
    
    private int level = 1;
    
    private int experience = 0;
    
    private int goldCoins = 100; // Moeda do jogo
    
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.AVENTUREIRO; // Papel do usuário
}
