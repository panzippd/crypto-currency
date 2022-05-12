package com.crypto.currency.common.http;

import com.crypto.currency.common.utils.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.util.List;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/12 22:29
 */
public class RxWebSocketClient {

    private final WebSocketClient webSocketClient;

    private RxWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    public static RxWebSocketClient create(HttpClient httpClient) {

        return new RxWebSocketClient(new ReactorNettyWebSocketClient(httpClient));
    }

    /**
     * Send multiple request contents
     *
     * @param endpoint
     * @param bodys
     * @return
     */
    public Flux<String> send(String endpoint, List<String> bodys) {

        final EmitterProcessor<String> output = EmitterProcessor.create();
        Mono<Void> wsc = null;
        if (CollectionUtils.isEmpty(bodys)) {
            wsc = webSocketClient.execute(URI.create(endpoint),
                session -> session.receive().limitRequest(1).map(r -> r.getPayloadAsText()).subscribeWith(output)
                    .then());
        } else {
            final int limit = bodys.size();
            wsc = webSocketClient.execute(URI.create(endpoint),
                session -> session.send(Flux.fromStream(bodys.stream()).map(session::textMessage))
                    .thenMany(session.receive().limitRequest(limit)).map(r -> r.getPayloadAsText())
                    .subscribeWith(output).then());
        }
        final Mono<Void> webSocket = wsc;
        return output.doOnSubscribe(s -> webSocket.subscribe());
    }

    /**
     * Send single request contents
     *
     * @param endpoint
     * @param body
     * @return
     */
    public Mono<String> send(String endpoint, String body) {

        final EmitterProcessor<String> output = EmitterProcessor.create();
        Mono<Void> wsc = null;
        if (StringUtils.isBlank(body)) {
            wsc = webSocketClient.execute(URI.create(endpoint),
                session -> session.receive().limitRequest(1).map(r -> r.getPayloadAsText()).subscribeWith(output)
                    .then());
        } else {
            wsc = webSocketClient.execute(URI.create(endpoint),
                session -> session.send(Mono.just(body).map(session::textMessage))
                    .thenMany(session.receive().limitRequest(1)).map(r -> r.getPayloadAsText()).subscribeWith(output)
                    .then());
        }
        final Mono<Void> webSocket = wsc;
        return output.doOnSubscribe(s -> webSocket.subscribe()).single();
    }
}
