package com.programacao_web.rpg_market.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex, HttpServletRequest request) {
        String message = ex.getMessage();
        String requestURI = request.getRequestURI();
        
        // Tratar IOExceptions específicas de conexão cancelada/interrompida
        if (message != null && (message.contains("Broken pipe") || 
                               message.contains("Connection reset") ||   
                               message.contains("connection was cancelled") ||
                               message.contains("conexão anulada pelo software no computador host") ||
                               message.contains("An existing connection was forcibly closed") ||
                               message.contains("Connection aborted"))) {
            
            // Log apenas como DEBUG para conexões canceladas - comportamento esperado
            logger.debug("Cliente desconectou durante requisição {}: {}", requestURI, message);
            
            // Para imagens e recursos estáticos, retornar resposta vazia
            if (requestURI.contains("/images/") || requestURI.contains("/css/") || requestURI.contains("/js/")) {
                return ResponseEntity.ok().build();
            }
        } else {
            // Para outros tipos de IOException, manter como WARN
            logger.warn("IOException em requisição {}: {}", requestURI, message);
        }
        
        // Retornar resposta adequada baseada no tipo de requisição
        if (requestURI.contains("/images/") || requestURI.contains("/css/") || requestURI.contains("/js/")) {
            return ResponseEntity.ok().build(); // Recursos estáticos retornam vazio
        } else {
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex) {
        logger.warn("404 - No handler found: {}", ex.getRequestURL());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error on request {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return "error/500";
    }
}
