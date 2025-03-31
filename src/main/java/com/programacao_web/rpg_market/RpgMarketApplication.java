package com.programacao_web.rpg_market;

import com.programacao_web.rpg_market.config.FileStorageProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(FileStorageProperties.class)
public class RpgMarketApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpgMarketApplication.class, args);
    }
    
    @Bean
    CommandLineRunner initFileStorage(FileStorageProperties fileStorageProperties) {
        return args -> {
            Path uploadDir = Paths.get(fileStorageProperties.getUploadDir());
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("Created upload directory at: " + uploadDir.toAbsolutePath());
            }
        };
    }
}
