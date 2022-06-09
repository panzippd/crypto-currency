package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.collector.util.SymbolUtils;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Panzi
 * @Description
 * @date 2022/6/9 23:27
 */
@Exchange(id = "1149", name = "Crypto.com Exchange")
@Slf4j
public class CryptoComExchangeService extends AExchange {

    private final static String URL = "https://api.crypto.com/v2/public/get-ticker";
    private final static String OD_URL = "https://api.crypto.com/v2/public/get-book?instrument_name=%s_%s&depth=150";
    /**
     * Provides information on all supported instruments (e.g. BTCUSD-PERP).
     */
    private final static String INSTRUMENTS_INFO_URL = "https://deriv-api.crypto.com/v1/public/get-instruments";
    /**
     * Retrieves candlesticks (k-line data history) over a given period for an instrument (e.g. BTCUSD-PERP).
     */
    private final static String INSTRUMENTS_DETAIL_URL = "https://deriv-api.crypto.com/v1/public/get-tickers";

    /**
     * @return
     */
    @Override
    public Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {

        return get(URL).map(result -> JacksonUtils.deserialize(result, CryptoComExchangeResponse.class))
            .map(this::toEntity);

    }

    private TickerEntity toEntity(CryptoComExchangeResponse response) {
        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .cmcTickers(Lists.newArrayListWithCapacity(response.getResult().getData().size()))
            .updatedTime(DateTimeUtils.nowUTC()).build();
        for (Market data : response.getResult().getData()) {
            Pair<String, String> p = SymbolUtils.splitWithUnderscore(data.getI());
            if (p == null) {
                continue;
            }
            tickerEntity.getCmcTickers().add(
                TickerEntity.CMCTicker.builder().baseSymbol(p.getKey()).mainSymbol(p.getValue()).quote(data.getA())
                    .mainVolume(data.getV().multiply(data.getA())).build());
        }
        return tickerEntity;
    }

    @Data
    public static class CryptoComExchangeResponse {
        private Integer code;
        private String method;
        private ResponseData result;
    }

    @Data
    public static class ResponseData {
        private List<Market> data;
    }

    @Data
    public static class Market {
        private String i;   //Instrument Name, e.g. BTC_USDT, ETH_CRO,
        private BigDecimal b;
        private BigDecimal k;
        private BigDecimal a;   //The price of the latest trade, null if there weren't any trades
        private Long t;
        private BigDecimal v;   //The total 24h traded volume,
        private BigDecimal h;
        private BigDecimal l;
        private BigDecimal c;
    }
}
