package com.programacao_web.rpg_market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${rpg.market.upload-dir:uploads/images}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Register resource handler for images
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/")
                .setCachePeriod(3600)
                .resourceChain(true);
                
        // Keep other resource handlers if needed
    }
}