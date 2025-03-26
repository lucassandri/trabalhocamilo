package com.programacao_web.rpg_market.service;

import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }
        
        User user = userOptional.get();
        
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }
    
    // Classe para estender UserDetails e incluir mais informações do usuário
    public static class CustomUserDetails extends org.springframework.security.core.userdetails.User {
        private final int level;
        private final int experience;
        private final String characterClass;

        public CustomUserDetails(User user, List<SimpleGrantedAuthority> authorities) {
            super(user.getUsername(), user.getPassword(), authorities);
            this.level = user.getLevel();
            this.experience = user.getExperience();
            this.characterClass = user.getCharacterClass();
        }

        public int getLevel() {
            return level;
        }

        public int getExperience() {
            return experience;
        }

        public String getCharacterClass() {
            return characterClass;
        }
    }
}