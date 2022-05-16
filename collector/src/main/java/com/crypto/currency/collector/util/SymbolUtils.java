package com.crypto.currency.collector.util;

import com.crypto.currency.common.utils.CollectionUtils;
import com.crypto.currency.data.vo.ParsedTradePairVO;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Panzi
 * @Description symbol util
 * @date 2022/5/16 21:44
 */
@Slf4j
@Component
public class SymbolUtils {
    private static final String CACHE_KEY_TEMPLATE_EXCHANGE_SYMBOL = "v3:worker-processor:exchange:symbol:%s";
    private WebClient marketServiceClient;

    @Autowired
    private RestTemplate restTemplate;
    @Value("${com.cmc.worker.market-service.url}")
    private String marketServiceUrl;

    @PostConstruct
    public void init() {
        marketServiceClient = WebClient.builder().baseUrl(marketServiceUrl).build();
    }

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
     * Tries to parse the symbols using the market service's /parse API.
     *
     * @param symbols a list of symbols to parse
     * @return mapping between to original symbol to the parsed pair
     */
    public Map<String, Pair<String, String>> advancedParse(List<String> symbols) {
        if (CollectionUtils.isEmpty(symbols)) {
            return Collections.emptyMap();
        }
        List<String> capitalizedSymbols = symbols.stream().map(String::toUpperCase).collect(Collectors.toList());
        RequestEntity<List<String>> requestEntity =
            new RequestEntity<>(capitalizedSymbols, HttpMethod.POST, URI.create(marketServiceUrl + "/markets/parse"));
        ResponseEntity<Map<String, List<ParsedTradePairVO>>> responseEntity =
            restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
            });
        Map<String, List<ParsedTradePairVO>> parsedPairs = responseEntity.getBody();

        if (CollectionUtils.isEmpty(parsedPairs)) {
            return Collections.emptyMap();
        }

        Map<String, Pair<String, String>> result = new HashMap<>(parsedPairs.size());
        for (Map.Entry<String, List<ParsedTradePairVO>> entry : parsedPairs.entrySet()) {
            List<ParsedTradePairVO> pairs = entry.getValue();
            if (pairs.size() == 1) {
                ParsedTradePairVO parsedTradePairVo = pairs.get(0);
                result.put(entry.getKey(),
                    Pair.of(parsedTradePairVo.getBaseSymbol(), parsedTradePairVo.getMainSymbol()));
            }
        }
        return result;
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
