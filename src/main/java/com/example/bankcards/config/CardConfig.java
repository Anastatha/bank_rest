package com.example.bankcards.config;

import com.example.bankcards.util.CardCryptoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CardConfig {

    @Value("${app.card.aes-key}")
    private String aesKey;

    @Bean
    public CardCryptoUtil cardCryptoUtil() {
        return new CardCryptoUtil(aesKey);
    }
}
