package com.programacao_web.rpg_market;

import com.programacao_web.rpg_market.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({FileStorageProperties.class})
public class RpgMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpgMarketApplication.class, args);
    }
}
