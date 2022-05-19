package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.common.utils.StringUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Panzi
 * @Description get binance exchange data
 * @date 2022/5/5 23:10
 */
@Slf4j
@Exchange(id = "270", name = "Binance")
public class BinanceService extends AExchange {
    // Global
    private static final String URL = "https://api.binance.com/api/v1/ticker/24hr";

    private static final String EX_INFO_URL = "https://api.binance.com/api/v3/exchangeInfo";

    private static final String OB_URL = "https://api.binance.com/api/v3/depth?limit=5000&symbol=";

    private static final String SWAP_URL = "https://fapi.binance.com/fapi/v1/ticker/24hr";

    private static final String SWAP_INFO = "https://fapi.binance.com/fapi/v1/exchangeInfo";

    private static final String BOOK_TICKER = "https://fapi.binance.com/fapi/v1/ticker/bookTicker";

    private static final String OPEN_INTEREST = "https://fapi.binance.com/fapi/v1/openInterest?symbol=%s";

    private static final String PERMIUM_INDEX = "https://fapi.binance.com/fapi/v1/premiumIndex";

    private static final String COIN_FUTURES_ECHANGE_INFO = "https://dapi.binance.com/dapi/v1/exchangeInfo";
    private static final String COIN_FUTURES_TICKER_24HR = "https://dapi.binance.com/dapi/v1/ticker/24hr";
    private static final String COIN_FUTURES_BOOK_TICKER = "https://dapi.binance.com/dapi/v1/ticker/bookTicker";
    private static final String COIN_FUTURES_OPEN_INTEREST = "https://dapi.binance.com/dapi/v1/openInterest?symbol=%s";
    private static final String COIN_FUTURES_PREMIUM_INDEX = "https://dapi.binance.com/dapi/v1/premiumIndex";

    // China
    // private final static String URL = "https://api.binancezh.com/api/v1/ticker/24hr";

    @Override
    public Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity task) {
        return Mono.zip(get(buildProxyUrl(URL, null)).map(
                m -> JacksonUtils.deserialize(m, new TypeReference<List<BinanceTickerData>>() {
                })), get(buildProxyUrl(EX_INFO_URL, null)).map(m -> JacksonUtils.deserialize(m, BinanceExInfoData.class)))
            .map(item -> this.toEntity(item, true));
    }

    private TickerEntity toEntity(Tuple2<List<BinanceTickerData>, BinanceExInfoData> tuple, boolean isSpot) {
        List<BinanceTickerData> tickers = tuple.getT1();
        BinanceExInfoData info = tuple.getT2();

        Map<String, BinanceExInfoSymbol> symbolMap = Maps.newHashMapWithExpectedSize(1500);
        try {
            info.getSymbols().forEach(item -> {
                symbolMap.put(item.getSymbol(), item);
            });
        } catch (Exception ex) {
            // nothing
        }

        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .cmcTickers(Lists.newArrayListWithCapacity(tickers.size())).updatedTime(DateTimeUtils.nowUTC()).build();

        List<TickerEntity.CMCTicker> list = tickerEntity.getCmcTickers();
        for (BinanceTickerData d : tickers) {
            Pair<String, String> p = symbolUtils.splitWithFullString(getExchangeId(), d.getSymbol());

            BinanceExInfoSymbol symbolInfo = symbolMap.get(d.getSymbol());
            if (symbolInfo != null) {
                if (p == null) {
                    // main = right = quote, base = left = base
                    p = Pair.of(symbolInfo.getBaseAsset(), symbolInfo.getQuoteAsset());
                }

                if (isSpot && !StringUtils.equalsIgnoreCase(symbolInfo.getStatus(), "TRADING")) {
                    p = null;
                }

            }

            TickerEntity.CMCTicker t;
            if (p != null) {
                t = TickerEntity.CMCTicker.builder().mainSymbol(p.getRight()).baseSymbol(p.getLeft())
                    .quote(d.getLastPrice()).mainVolume(d.getVolume().multiply(d.getLastPrice())).build();
            } else {
                t = TickerEntity.CMCTicker.builder().baseSymbol(d.getSymbol()).quote(d.getLastPrice())
                    .mainVolume(d.getVolume().multiply(d.getLastPrice())).build();
            }
            list.add(t);

        }
        return tickerEntity;
    }

    @Data
    public static class BinanceTickerData {

        private final BigDecimal priceChange;
        private final BigDecimal priceChangePercent;
        private final BigDecimal weightedAvgPrice;
        private final BigDecimal prevClosePrice;
        private final BigDecimal lastPrice;
        private final BigDecimal lastQty;
        private final BigDecimal bidPrice;
        private final BigDecimal bidQty;
        private final BigDecimal askPrice;
        private final BigDecimal askQty;
        private final BigDecimal openPrice;
        private final BigDecimal highPrice;
        private final BigDecimal lowPrice;
        private final BigDecimal volume;
        private final BigDecimal quoteVolume;
        private final BigDecimal baseVolume;
        private final long openTime;
        private final long closeTime;
        private final long firstId;
        private final long lastId;
        private final long count;
        private final String symbol;
        private final String pair;
    }

    @Data
    public static class BinanceExInfoData {

        private String timezone;
        private Long serverTime;
        private List<BinanceExInfoSymbol> symbols;
    }

    @Data
    public static class BinanceExInfoSymbol {
        private String symbol;
        private String pair;
        private String contractType;
        private Long deliveryDate;
        private Long onboardDate;
        private String contractStatus;
        private BigDecimal contractSize;
        private String marginAsset;
        private BigDecimal maintMarginPercent;
        private BigDecimal requiredMarginPercent;
        private BigDecimal triggerProtect;

        private String status;
        private String baseAsset;
        private String quoteAsset;

    }
}
