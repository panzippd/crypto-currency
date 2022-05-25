package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.collector.util.SymbolUtils;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.ExtUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.crypto.currency.data.enums.DataType;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Panzi
 * @Description Kraken exchange
 * @date 2022/5/25 23:25
 * @Description API Docs : https://support.kraken.com/hc/en-us/articles/360022839531-Tickers
 * symbol : https://support.kraken.com/hc/en-us/articles/360022835891-Ticker-symbols
 * FI = Inverse Futures
 * FV = Vanilla Futures
 * PI = Perpetual Inverse Futures
 * PV = Perpetual Vanilla Futures
 * IN = Real Time Index
 * RR = Reference Rate
 */
@Slf4j
@Exchange(id = "24", name = "Kraken")
public class KrakenService extends AExchange {

    private final static String URL = "https://api.kraken.com/0/public/AssetPairs";

    /**
     * POST，param >>>> {"pair": "XXBTZUSD"}
     */
    private final static String ORDER_BOOK_URL = "https://api.kraken.com/0/public/Depth";

    private final static String MARKET_DATA_URL = "https://api.kraken.com/0/public/Ticker?pair=";
    private final static String SWAP_URL = "https://futures.kraken.com/derivatives/api/v3/tickers";
    private final static String INSTRUMENTS_URL = "https://futures.kraken.com/derivatives/api/v3/instruments";

    @Override
    protected Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {

        return get(URL).map(m -> JacksonUtils.deserialize(JacksonUtils.readTree(m).get("result").asText(),
            (new TypeReference<Map<String, KrakenTickerData>>() {
            }))).flatMap(resp -> Flux.fromIterable(resp.entrySet()).filter(rg -> {
            Pair<String, String> namePair = SymbolUtils.splitWithSymbol(rg.getValue().getWsname(), "/");
            return namePair != null;
        }).flatMap(t -> getMarketOne(t.getValue().getAltname()).map(market -> {
            Pair<BigDecimal, BigDecimal> dataPair = transSymbolData(market);
            Pair<String, String> symbolPair = SymbolUtils.splitWithSymbol(t.getValue().getWsname(), "/");
            return TickerEntity.CMCTicker.builder().baseSymbol(symbolPair.getLeft()).mainSymbol(symbolPair.getRight())
                .quote(dataPair.getLeft()).mainVolume(dataPair.getRight()).build();
        })).collectList().map(m -> TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .dataType(DataType.SPOT).cmcTickers(m).updatedTime(DateTimeUtils.nowUTC()).build()));

    }

    /**
     * get one market detail
     *
     * @param marketName
     * @return
     */
    private Mono<OneMarket> getMarketOne(String marketName) {
        return get(MARKET_DATA_URL + marketName).map(m -> JacksonUtils.deserialize(m, OneMarket.class))
            .defaultIfEmpty(new OneMarket());
    }

    private Pair<BigDecimal, BigDecimal> transData(Map<String, String[]> data) {
        BigDecimal mainVolume = BigDecimal.ZERO;
        BigDecimal quote = BigDecimal.ZERO;
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            if ("c".equals(entry.getKey())) {
                quote = ExtUtils.parseBigDecimal(entry.getValue()[0]);
            }
            if ("v".equals(entry.getKey())) {
                mainVolume = ExtUtils.parseBigDecimal(entry.getValue()[1]);
            }
        }
        return Pair.of(quote, mainVolume.multiply(quote));
    }

    private Pair<BigDecimal, BigDecimal> transSymbolData(OneMarket data) {
        Pair pair = null;
        if (data == null) {
            return pair;
        }
        for (Map.Entry<String, Map<String, String[]>> entry : data.getResult().entrySet()) {
            pair = transData(entry.getValue());
        }
        return pair;
    }

    @Data
    public static class OneMarket {
        private Map<String, Map<String, String[]>> result;
    }

    @Data
    public static class KrakenData {

        private Map<String, KrakenTickerData> result;
    }

    @Data
    public static class KrakenTickerData {

        private String altname;
        private String wsname;
        private String aclass_base;
        private String base;
        private String aclass_quote;
        private String quote;
        private String lot;
        private String pair_decimals;
        private String lot_decimals;
        private String lot_multiplier;
        private String fee_volume_currency;

        private Integer margin_call;
        private Integer margin_stop;

        private Integer[] leverage_buy;

        private Integer[] leverage_sell;

    }
}
