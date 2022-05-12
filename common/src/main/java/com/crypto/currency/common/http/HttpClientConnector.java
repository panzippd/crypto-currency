package com.crypto.currency.common.http;

import com.crypto.currency.common.exception.BusinessException;
import com.crypto.currency.common.utils.ExtUtils;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/12 22:30
 */
public class HttpClientConnector {

    private HttpClientConnector() {

    }

    /**
     * configuration httpclient
     *
     * @param connectTimeout
     * @param readTimeout
     * @param writeTimeout
     * @return
     */
    @SneakyThrows
    private static HttpClient createHttpClient(boolean isSecure, Integer connectTimeout, Integer readTimeout,
        Integer writeTimeout) {

        HttpClient client = HttpClient.create().followRedirect(true).tcpConfiguration(
            c -> c.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ExtUtils.getDefaultValue(connectTimeout, 30000))
                .doOnConnected(
                    conn -> conn.addHandlerLast(new ReadTimeoutHandler(ExtUtils.getDefaultValue(readTimeout, 10)))
                        .addHandlerLast(new WriteTimeoutHandler(ExtUtils.getDefaultValue(writeTimeout, 10)))));
        if (isSecure) {
            SslContext sslContext = null;
            try {
                sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                final SslContext finalSslContext = sslContext;
                client.secure(sslContextSpec -> sslContextSpec.sslContext(finalSslContext));
            } catch (SSLException e) {
                BusinessException.throwIfUnchecked(e);
            }
        }
        return client;
    }

    public static Builder create() {

        return new Builder();
    }

    public static class Builder {

        private boolean isSecure;
        private Integer connectTimeout;
        private Integer readTimeout;
        private Integer writeTimeout;

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

        public HttpClient build() {
            return createHttpClient(isSecure, connectTimeout, readTimeout, writeTimeout);
        }
    }
}
