package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.collector.util.SymbolUtils;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/31 22:32
 */
@Slf4j
@Exchange(id = "302", name = "Gate.io")
public class GateIoService extends AExchange {

    private static final String URL = "http://data.gate.io/api2/1/tickers";
    private static final String ORDER_BOOK_URL = "https://data.gateio.life/api2/1/orderBook/%s_%s";

    private static final String BTC_TICKER_URL = "https://api.gateio.ws/api/v4/futures/btc/tickers";
    private static final String USDT_TICKER_URL = "https://api.gateio.ws/api/v4/futures/usdt/tickers";
    private static final String BTC_CONTRACT_STATS_URL =
        "https://api.gateio.ws/api/v4/futures/btc/contract_stats?contract=%s&limit=1";
    private static final String BTC_CONTRACT_URL = "https://api.gateio.ws/api/v4/futures/btc/contracts";
    private static final String USDT_CONTRACT_URL = "https://api.gateio.ws/api/v4/futures/usdt/contracts";
    private static final String USDT_CONTRACT_STATS_URL =
        "https://api.gateio.ws/api/v4/futures/usdt/contract_stats?contract=%s&limit=1";

    private static final String FUTURES_TICKER_URL = "https://api.gateio.ws/api/v4/delivery/usdt/tickers";
    private static final String FUTURES_CONTRACT_URL = "https://api.gateio.ws/api/v4/delivery/usdt/contracts";

    @Override
    protected Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {
        return get(URL).map(m -> JacksonUtils.deserialize(m, (new TypeReference<Map<String, GateIoTickerData>>() {
        }))).map(this::toEntity);
    }

    private TickerEntity toEntity(Map<String, GateIoTickerData> response) {

        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .cmcTickers(Lists.newArrayListWithCapacity(response.size())).updatedTime(DateTimeUtils.nowUTC()).build();

        for (Map.Entry<String, GateIoTickerData> item : response.entrySet()) {
            GateIoTickerData d = item.getValue();
            String symbol = StringUtils.upperCase(item.getKey());

            Pair<String, String> p = SymbolUtils.splitWithUnderscore(symbol);
            if (p == null) {
                continue;
            }

            tickerEntity.getCmcTickers().add(
                TickerEntity.CMCTicker.builder().baseSymbol(p.getLeft()).mainSymbol(p.getRight()).quote(d.getLast())
                    .mainVolume(d.getBaseVolume()).build());
        }
        return tickerEntity;
    }

    @Data
    public static class GateIoTickerData {
        private String symbol;
        private String result;
        private BigDecimal last;
        private BigDecimal lowestAsk;
        private BigDecimal highestBid;
        private BigDecimal percentChange;
        private BigDecimal baseVolume;
        private BigDecimal quoteVolume;
        private BigDecimal high24hr;
        private BigDecimal low24hr;
    }
}
