package com.programacao_web.rpg_market;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class RpgMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpgMarketApplication.class, args);
    }
    
    @Bean
    CommandLineRunner initFileStorage() {
        return args -> {
            Path uploadDir = Paths.get("uploads/images/");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("Created upload directory at: " + uploadDir.toAbsolutePath());
            }
        };
    }
}
