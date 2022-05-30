package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.collector.util.SymbolUtils;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.ExtUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/27 23:35
 * orderBook, https://api-futures.kucoin.com/api/v1/level2/snapshot?symbol=XBTUSDM
 * other
 * https://api-futures.kucoin.com/api/v1/contracts/XBTUSDM
 * https://api-futures.kucoin.com/api/v1/contracts/ETHUSDTM
 */
@Slf4j
@Exchange(id = "311", name = "KuCoin")
public class KuCoinService extends AExchange {

    private static final String URL = "https://api.kucoin.com/api/v1/market/allTickers";
    private final static String ORDER_BOOK_URL =
        "https://api.kucoin.com/api/v1/market/orderbook/level2_100?symbol=%s-%s";
    private static final String FUTURE_URL = "https://api-futures.kucoin.com/api/v1/contracts/active";
    private static final String FUTURE_TICKER_URL = "https://api-futures.kucoin.com/api/v1/ticker?symbol=%s";
    private static final String FUNDING_RATE_URL = "https://api-futures.kucoin.com/api/v1/funding-rate/%s/current";

    @Override
    public Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {
        return get(URL).map(m -> JacksonUtils.deserialize(m, KuCoinTickerData.class)).map(this::toEntity);
    }

    private TickerEntity toEntity(KuCoinTickerData response) {
        List<KCTicker> tickers = response.getData().getTicker();

        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .cmcTickers(Lists.newArrayListWithCapacity(tickers.size())).updatedTime(DateTimeUtils.nowUTC()).build();

        for (KCTicker item : tickers) {
            String symbol = item.getSymbol();
            if (StringUtils.isEmpty(symbol)) {
                continue;
            }
            Pair<String, String> p = SymbolUtils.splitWithDash(symbol);
            if (p == null) {
                continue;
            }

            tickerEntity.getCmcTickers().add(
                TickerEntity.CMCTicker.builder().baseSymbol(p.getLeft()).mainSymbol(p.getRight()).quote(item.getLast())
                    .mainVolume(ExtUtils.getNotNull(item.getLast()).multiply(item.getVol())).build());
        }
        return tickerEntity;
    }

    @Data
    public static class KuCoinTickerData {
        private String code;
        private KCTickers data;
    }

    @Data
    public static class KCTickers {
        private List<KCTicker> ticker;
        private Long time;
    }

    @Data
    public static class KCTicker {
        private String symbol;
        private BigDecimal vol;
        private BigDecimal high;
        private BigDecimal last;
        private BigDecimal low;
        private BigDecimal buy;
        private BigDecimal sell;
        private BigDecimal changePrice;
        private String symbolName;
        private BigDecimal averagePrice;
        private BigDecimal changeRate;
        private BigDecimal volValue;
    }
}
