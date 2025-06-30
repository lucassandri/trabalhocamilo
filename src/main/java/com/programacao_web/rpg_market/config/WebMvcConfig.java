package com.programacao_web.rpg_market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        try {
            logger.info("Iniciando configuração de resource handlers. Diretório lido: '{}'", uploadDir);

            if (uploadDir != null && !uploadDir.isEmpty()) {
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath();

                String resourceLocation = uploadPath.toUri().toString();

                logger.info("Configurando o resource handler para o padrão /uploads/** no caminho: {}", resourceLocation);

                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(resourceLocation); 
            } else {
                logger.error("O diretório de upload (app.upload.dir) é nulo ou vazio!");
            }

        } catch (Exception e) {
            logger.error("Erro crítico ao configurar o resource handler: {}", e.getMessage(), e);
        }
    }
}