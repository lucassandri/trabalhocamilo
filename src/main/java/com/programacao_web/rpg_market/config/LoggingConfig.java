package com.programacao_web.rpg_market.config;

import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Configuração personalizada de logging para otimizar os logs da aplicação
 * Mantém logs importantes de erro, mas silencia logs repetitivos e desnecessários
 */
@Configuration
public class LoggingConfig {

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        LoggingSystem loggingSystem = LoggingSystem.get(ClassLoader.getSystemClassLoader());
        
        // Silencia logs específicos que geram muito ruído (nível ERROR para ver apenas erros críticos)
        loggingSystem.setLogLevel("org.springframework.security.web.FilterChainProxy", LogLevel.OFF);
        loggingSystem.setLogLevel("org.springframework.web.servlet.DispatcherServlet", LogLevel.OFF);
        loggingSystem.setLogLevel("org.springframework.security.web.authentication.AnonymousAuthenticationFilter", LogLevel.OFF);
        loggingSystem.setLogLevel("org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor", LogLevel.OFF);
        loggingSystem.setLogLevel("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping", LogLevel.OFF);
        
        // Silencia completamente logs de recursos estáticos
        loggingSystem.setLogLevel("org.springframework.web.servlet.resource", LogLevel.OFF);
        loggingSystem.setLogLevel("org.springframework.web.servlet.handler", LogLevel.OFF);
        
        // Mantém logs importantes da aplicação
        loggingSystem.setLogLevel("com.programacao_web.rpg_market", LogLevel.INFO);
        
        // Garante que logs importantes do sistema sejam visíveis
        loggingSystem.setLogLevel("org.springframework.boot", LogLevel.WARN);
        loggingSystem.setLogLevel("org.springframework.data.mongodb", LogLevel.WARN);
    }
}
