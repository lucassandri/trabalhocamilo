package com.programacao_web.rpg_market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RpgMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpgMarketApplication.class, args);
    }

}
