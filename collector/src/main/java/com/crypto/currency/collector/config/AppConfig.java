package com.crypto.currency.collector.config;

import com.crypto.currency.common.http.AsynHttpClient;
import com.crypto.currency.common.http.HttpClientConnector;
import com.crypto.currency.common.http.RxWebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/12 22:28
 */
public class AppConfig {

    @Bean
    public WebClient createWebClient() {

        return AsynHttpClient.builder().isSecure(true).acquireTimeout(50000).maxConnections(100000).readTimeout(30)
            .writeTimeout(30).connectTimeout(30000).build();
    }

    @Bean
    @ConditionalOnBean(WebClient.class)
    public AsynHttpClient.DefaultWebClient createDefaultWebClient(@Autowired WebClient client) {

        return new AsynHttpClient.DefaultWebClient(client);
    }

    @Bean
    public RxWebSocketClient createWebSocketClient() {

        return RxWebSocketClient.create(
            HttpClientConnector.create().isSecure(true).connectTimeout(60000).readTimeout(15).writeTimeout(15).build());
    }
}
