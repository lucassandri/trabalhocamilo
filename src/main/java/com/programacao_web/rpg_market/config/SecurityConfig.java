package com.programacao_web.rpg_market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Páginas públicas
                .requestMatchers("/", "/mercado/**", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/error/**").permitAll()
                .requestMatchers("/item/{id}").permitAll() // Visualização de itens é pública
                .requestMatchers("/aventureiro/registrar").permitAll()
                
                // Páginas que requerem autenticação
                .requestMatchers("/item/novo").authenticated()
                .requestMatchers("/item/{id}/editar").authenticated()
                .requestMatchers("/item/{id}/comprar").authenticated()
                .requestMatchers("/item/{id}/lance").authenticated()
                .requestMatchers("/aventureiro/**").authenticated()
                .requestMatchers("/transacao/**").authenticated()
                
                // Páginas administrativas (apenas para role MESTRE)
                .requestMatchers("/admin/**").hasRole("MESTRE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/mercado", true)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/error/403")
            );
            
        return http.build();
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}