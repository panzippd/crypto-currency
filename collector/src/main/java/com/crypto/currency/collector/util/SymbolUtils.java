package com.crypto.currency.collector.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author Panzi
 * @Description symbol util
 * @date 2022/5/16 21:44
 */
@Slf4j
@Component
public class SymbolUtils {
    private static final String CACHE_KEY_TEMPLATE_EXCHANGE_SYMBOL = "v3:worker-processor:exchange:symbol:%s";

    @Autowired
    private RestTemplate restTemplate;

    private static final String UNDERSCORE = "_";
    private static final String HYPHEN = "-";
    private static final String COLON = ":";
    private static final String SLASH = "/";

    /**
     * Splits the symbol pair string without separator, for example: BTCUSDT BTCBUSD, BTCETH.
     *
     * @param symbolPair the original symbol pair string
     * @param exchangeId the id of the exchange
     * @return the parsed {@link Pair} object; null if the string cannot be parsed
     */
    public Pair<String, String> splitWithFullString(Integer exchangeId, String symbolPair) {
        return splitWithFullString(exchangeId, symbolPair, false);
    }

    /**
     * Splits the symbol pair string without separator, for example: BTCUSDT BTCBUSD, BTCETH.
     *
     * @param symbolPair       the original symbol pair string
     * @param exchangeId       the id of the exchange
     * @param isSymbolReversed whether the base symbol and main symbol are in a reversed order
     * @return the parsed {@link Pair} object; null if the string cannot be parsed
     */
    public Pair<String, String> splitWithFullString(Integer exchangeId, String symbolPair, boolean isSymbolReversed) {

        if (StringUtils.isBlank(symbolPair)) {
            log.error("symbolPair is empty");
            return null;
        }

        Map<String, SymbolPair> exchangeCache = getExchangeSymbolCache(exchangeId, isSymbolReversed);
        SymbolPair pairMatched = exchangeCache.get(symbolPair);
        if (pairMatched != null) {
            return Pair.of(pairMatched.getBase(), pairMatched.getMain());
        } else {
            log.warn("Cannot find the symbol pair: {} for exchange: {}", symbolPair, exchangeId);
            //            MarketEntity newMarket = new NewMarketVO(exchangeId, symbolPair);
            //            List<NewMarketVO> newMarkets = List.of(newMarket);
            //            // we don't really care about the result here
            //            marketServiceClient.post().uri("/markets/log").bodyValue(newMarkets).retrieve().bodyToFlux(String.class)
            //                .subscribe();
            return null;
        }
    }

    //todo
    private Map<String, SymbolPair> getExchangeSymbolCache(Integer exchangeId, boolean isSymbolReversed) {
        return null;
    }

    /**
     * BTC_USDT
     *
     * @param symbol
     * @return
     */
    public static Pair<String, String> splitWithUnderscore(String symbol) {
        return splitWithSymbol(symbol, UNDERSCORE);
    }

    /**
     * BTC-USDT
     *
     * @param symbol
     * @return
     */
    public static Pair<String, String> splitWithDash(String symbol) {
        return splitWithSymbol(symbol, HYPHEN);
    }

    /**
     * BTC:USDT
     *
     * @param symbol
     * @return
     */
    public static Pair<String, String> splitWithColon(String symbol) {
        return splitWithSymbol(symbol, COLON);
    }

    /**
     * BTC/USDT
     *
     * @param symbol
     * @return
     */
    public static Pair<String, String> splitWithSlash(String symbol) {
        return splitWithSymbol(symbol, SLASH);
    }

    /**
     * BTC_USDT, BTC/USDT
     *
     * @param symbol
     * @param separator
     * @return
     */
    public static Pair<String, String> splitWithSymbol(String symbol, String separator) {
        if (StringUtils.isBlank(symbol) || StringUtils.isBlank(separator)) {
            log.error("Symbol or separator is empty");
            return null;
        }

        String[] parts = StringUtils.split(symbol, separator);
        if (parts.length != 2) {
            log.error("Symbol  can not been split to two part by _ ---> {}", symbol);
            return null;
        }
        return Pair.of(parts[0], parts[1]);
    }

    /**
     * btcusdt hbarbtc
     *
     * @param symbol
     * @return
     */
    public static Pair<String, String> splitWithLowercase(String symbol) {
        if (StringUtils.isBlank(symbol)) {
            log.error("Symbol is empty");
            return null;
        }
        String upperCase = StringUtils.upperCase(symbol);
        int length = 3;
        if (upperCase.length() > 6) {
            length = 4;
        }
        Iterable<String> result = Splitter.fixedLength(length).split(upperCase);
        String[] parts = Iterables.toArray(result, String.class);
        return Pair.of(parts[0], parts[1]);
    }

    /**
     * @param symbol  NGCUSD
     * @param pattern USD
     * @return NGC
     */
    public static String subSymbol(String symbol, String pattern) {
        if (StringUtils.isBlank(symbol)) {
            log.error("Symbol is empty");
            return null;
        }
        return symbol.substring(0, symbol.length() - pattern.length());
    }

    @Data
    public static class SymbolPair {
        private String main;
        private String base;

        public SymbolPair() {
        }

        public SymbolPair(String main, String base) {
            this.main = main;
            this.base = base;
        }
    }

    //    public static <T extends ISymbol> Map<String, T> toSymbolMap(JsonNode symbolsNode, Class<T> clazz) {
    //        Map<String, T> map;
    //        if (!symbolsNode.isEmpty() && symbolsNode.isArray()) {
    //            map = Maps.newHashMapWithExpectedSize(symbolsNode.size());
    //            List<T> symbolList = JSON.parseArray(symbolsNode.toString(), clazz);
    //            for (T symbol : symbolList) {
    //                map.put(symbol.getSymbol(), symbol);
    //            }
    //        } else {
    //            map = Collections.emptyMap();
    //        }
    //        return map;
    //    }
}
