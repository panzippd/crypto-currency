package com.crypto.currency.common.http;

import com.crypto.currency.common.exception.BusinessException;
import com.crypto.currency.common.utils.ExtUtils;
import com.crypto.currency.common.utils.StringUtils;
import com.google.common.base.Preconditions;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;
import reactor.util.retry.Retry;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Panzi
 * @Description Reactor Web Client component
 * @date 2022/4/29 16:58
 */
public class AsynHttpClient {
    /**
     * builder
     *
     * @return
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean isSecure;
        private Integer connectTimeout;
        private Integer readTimeout;
        private Integer writeTimeout;
        private int workerCounter;
        private int maxConnections;
        private long acquireTimeout;

        public Builder isSecure(boolean isSecure) {

            this.isSecure = isSecure;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {

            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {

            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(int writeTimeout) {

            this.writeTimeout = writeTimeout;
            return this;
        }

        public Builder workerCounter(int workerCounter) {

            this.workerCounter = workerCounter;
            return this;
        }

        public Builder maxConnections(int maxConnection) {

            this.maxConnections = maxConnection;
            return this;
        }

        public Builder acquireTimeout(int acquireTimeout) {

            this.acquireTimeout = acquireTimeout;
            return this;
        }

        public WebClient build() {

            return WebClient.builder()
                .defaultRequest(c -> c.accept(MediaType.APPLICATION_JSON).attribute("content-encoding", "gzip"))
                .clientConnector(getConnector(this.isSecure, this.connectTimeout, this.readTimeout, this.writeTimeout))
                .exchangeStrategies(exchangeStrategies()).build();
        }

        /**
         * decoder and encoder
         *
         * @return
         */
        private ExchangeStrategies exchangeStrategies() {

            return ExchangeStrategies.builder().codecs(configurer -> {
                // configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(JacksonUtils.getMapper()));
                // configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(JacksonUtils.getMapper()));
                configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024);
                // configurer.customCodecs().register();
            }).build();
        }

        /**
         * declare netty Connector client
         * product solution:https://stackoverflow.com/questions/53341607/how-to-configure-a-reactive-webclient-to-use-2-way-tls?noredirect=1&lq=1
         * Now "InsecureTrustManagerFactory" solution: Suitable for test environments only
         *
         * @return
         */
        private ReactorClientHttpConnector getConnector(boolean isSecure, Integer connectTimeout, Integer readTimeout,
            Integer writeTimeout) {

            return new ReactorClientHttpConnector(getReactorResourceFactory(), client -> {
                client = client.followRedirect(true).tcpConfiguration(
                    c -> c.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ExtUtils.getDefaultValue(connectTimeout, 30000))
                        .doOnConnected(conn -> conn.addHandlerLast(
                                new ReadTimeoutHandler(ExtUtils.getDefaultValue(readTimeout, 60)))
                            .addHandlerLast(new WriteTimeoutHandler(ExtUtils.getDefaultValue(writeTimeout, 60)))));
                client = client.httpResponseDecoder(spec -> spec.maxHeaderSize(64 * 1024));
                if (isSecure) {
                    SslContext sslContext = null;
                    try {
                        sslContext = SslContextBuilder.forClient().protocols("TLSv1.2")
                            .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                        final SslContext finalSslContext = sslContext;
                        client = client.secure(sslContextSpec -> sslContextSpec.sslContext(finalSslContext));
                    } catch (SSLException e) {
                        BusinessException.throwIfUnchecked(e);
                    }
                }
                return client;
            });
        }

        /**
         * declare reactor workerCount;
         *
         * @return
         */
        private ReactorResourceFactory getReactorResourceFactory() {

            ReactorResourceFactory resourceFactory = new ReactorResourceFactory();
            resourceFactory.setUseGlobalResources(true);
            int cpuCore = Math.max(Runtime.getRuntime().availableProcessors(), 8);
            resourceFactory.setConnectionProvider(ConnectionProvider.builder("Worker-Nio-Client")
                .maxConnections(Math.max((cpuCore * 2) + 1, this.maxConnections))
                .pendingAcquireTimeout(Duration.ofMillis(Math.max(this.acquireTimeout, 60000)))
                .pendingAcquireMaxCount(-1).build());
            resourceFactory.setLoopResources(
                LoopResources.create("Worker-Nio-Client", Math.max(cpuCore * 2, this.workerCounter), true));
            return resourceFactory;
        }
    }

    public static class DefaultWebClient {

        private final WebClient webClient;

        public DefaultWebClient(WebClient client) {

            this.webClient = client;
        }

        /**
         * method = get
         *
         * @param url
         * @param queryString
         * @return
         */
        public Mono<ResponseEntity<String>> get(String url, String queryString, Consumer<HttpHeaders> header) {

            Preconditions.checkArgument(StringUtils.isNotBlank(url));
            return webClient.mutate().defaultRequest(requestHeadersSpec -> requestHeadersSpec.headers(header))
                .baseUrl(url + StringUtils.defaultString(queryString, StringUtils.EMPTY))
                .filter(contentTypeInterceptor()).build().get().exchange()
                .retryWhen(Retry.backoff(3L, Duration.ofMillis(1013L))).flatMap(m -> m.toEntity(String.class));
        }

        /**
         * method = get
         *
         * @param url
         * @param queryString
         * @return
         */
        public Mono<ResponseEntity<String>> getForQueryString(String url,
            LinkedMultiValueMap<String, String> queryString) {

            Preconditions.checkArgument(StringUtils.isNotBlank(url));
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url).queryParams(queryString);
            UriComponents uriComponents = builder.build().encode();
            return webClient.mutate().filter(contentTypeInterceptor()).build().get().uri(uriComponents.toUri())
                .exchange().retryWhen(Retry.backoff(3L, Duration.ofMillis(1013L)))
                .flatMap(m -> m.toEntity(String.class));
        }

        /**
         * filter content-type : text-plain,and change it to text/plain
         *
         * @return
         */
        private ExchangeFilterFunction contentTypeInterceptor() {

            return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {

                List<String> values;
                if (clientResponse.headers() != null
                    && (values = clientResponse.headers().header("Content-Type")) != null) {

                    for (String value : values) {
                        if (StringUtils.containsAny(StringUtils.lowerCase(value), "text-plain", "text")) {
                            return Mono.just(ClientResponse.from(clientResponse)
                                .headers(headers -> headers.remove(HttpHeaders.CONTENT_TYPE))
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                                .body(clientResponse.body(BodyExtractors.toDataBuffers())).build());
                        }
                        if (StringUtils.containsIgnoreCase(value, "application/json charset=UTF-8")) {
                            return Mono.just(ClientResponse.from(clientResponse)
                                .headers(headers -> headers.remove(HttpHeaders.CONTENT_TYPE))
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .body(clientResponse.body(BodyExtractors.toDataBuffers())).build());
                        }
                    }
                }
                return Mono.just(clientResponse);
            });
        }

        /**
         * get request
         *
         * @param url
         * @return
         */
        public Mono<ResponseEntity<String>> get(String url) {

            return get(url, null, (httpHeaders -> {
            }));
        }

        /**
         * no queryString get request
         *
         * @param url
         * @param header
         * @return
         */
        public Mono<ResponseEntity<String>> get(String url, Consumer<HttpHeaders> header) {

            return get(url, null, header);
        }

        /**
         * method = post
         *
         * @param url
         * @param bodyValue
         */
        public Mono<ResponseEntity<String>> post(String url, Object bodyValue, Consumer<HttpHeaders> header) {

            return webClient.mutate().defaultRequest(requestHeadersSpec -> requestHeadersSpec.headers(header))
                .baseUrl(url).build().post().bodyValue(bodyValue).exchange()
                .retryWhen(Retry.backoff(3L, Duration.ofMillis(1013L))).flatMap(m -> m.toEntity(String.class));
        }

        /**
         * post request
         * default content-type:application/json
         *
         * @param url
         * @param bodyValue
         * @return
         */
        public Mono<ResponseEntity<String>> post(String url, Object bodyValue) {

            return post(url, bodyValue, (h) -> {
                h.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            });
        }

        /**
         * no body post request
         *
         * @param url
         * @return
         */
        public Mono<ResponseEntity<String>> post(String url) {

            return post(url, new Object(), (h) -> {
            });
        }
    }
}
