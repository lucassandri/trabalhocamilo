package com.programacao_web.rpg_market.service;

import com.programacao_web.rpg_market.model.Transaction;
import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.repository.TransactionRepository;
import com.programacao_web.rpg_market.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username já existe!");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email já está em uso!");
        }
        
        // Codifica a senha antes de salvar
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Transactional
    public void addExperience(User user, int exp) {
        user.setExperience(user.getExperience() + exp);
        
        // Verifica se subiu de nível (100 exp por nível)
        int newLevel = (user.getExperience() / 100) + 1;
        if (newLevel > user.getLevel()) {
            user.setLevel(newLevel);
            user.setGoldCoins(user.getGoldCoins() + 50); // Recompensa por subir de nível
        }
        
        userRepository.save(user);
    }
    
    // Métodos faltantes
    public List<Transaction> getUserPurchases(User user) {
        return transactionRepository.findByBuyer(user);
    }
    
    public List<Transaction> getUserSales(User user) {
        return transactionRepository.findBySeller(user);
    }
    
    /**
     * Altera a senha do usuário
     */
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword, String confirmPassword) {
        try {
            // Verifica se a senha atual está correta
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Senha atual incorreta");
            }
            
            // Verifica se a nova senha e a confirmação são iguais
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("A nova senha e a confirmação não correspondem");
            }
            
            // Verifica se a nova senha tem pelo menos 6 caracteres
            if (newPassword.length() < 6) {
                throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres");
            }
            
            // Atualiza a senha
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            System.out.println("Senha alterada com sucesso para o usuário: " + user.getUsername());
        } catch (Exception e) {
            System.err.println("Erro ao alterar senha: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Atualiza o perfil do usuário
     */
    @Transactional
    public void updateProfile(User currentUser, User updatedUser) {
        // Verifica se o email foi alterado e se já está em uso
        if (!currentUser.getEmail().equals(updatedUser.getEmail()) && 
            userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Este email já está sendo usado por outro aventureiro");
        }
        
        // Atualiza apenas os campos permitidos
        currentUser.setEmail(updatedUser.getEmail());
        currentUser.setCharacterClass(updatedUser.getCharacterClass());
        
        userRepository.save(currentUser);
    }
}
