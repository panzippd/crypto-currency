package com.crypto.currency.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author Panzi
 * @Description defult config
 * @date 2022/4/28 18:27
 */
@Configuration
public class AppConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb")
    @Order(0)
    public MultipleMongoProperties connectionSettings() {
        return new MultipleMongoProperties();
    }
    
}
