package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.util.SymbolUtils;
import com.crypto.currency.common.exception.BusinessException;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Panzi
 * @Description the exchange Coinbase
 * @date 2022/5/6 23:28
 */
public class CoinbaseProService extends AExchange {

    private final static String URL = "https://discover.coinmarketcap.supply/exchange/89";

    private static final String OD_URL = "https://api.pro.coinbase.com/products/";

    private final static String PAIRS_URL = "https://api.pro.coinbase.com/products/";

    private final static String TICKER_URL = "https://api.pro.coinbase.com/products/%s/ticker";

    @Override
    protected Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {
        return get(PAIRS_URL).map(r -> JacksonUtils.readTree(r)).flatMapIterable(this::getPairList)
            .flatMap(item -> get(buildProxyUrl(String.format(TICKER_URL, item), null)).map(m -> {
                CoinbaseTickerData coinbaseTickerData = JacksonUtils.deserialize(m, CoinbaseTickerData.class);
                Pair<String, String> pair = SymbolUtils.splitWithDash(item);
                coinbaseTickerData.setBaseSymbol(pair.getLeft());
                coinbaseTickerData.setMainSymbol(pair.getRight());
                return coinbaseTickerData;
            })).collectList().map(this::toEntity);
    }

    private List<String> getPairList(JsonNode jsonNode) {
        var jsons = jsonNode.elements();
        if (jsons == null) {
            BusinessException.throwIfUnkown();
        }
        List<String> pairList = new ArrayList<>();
        while (jsons.hasNext()) {
            JsonNode son = jsons.next();
            pairList.add(son.get("id").textValue());
        }
        return pairList;
    }

    private TickerEntity toEntity(List<CoinbaseTickerData> response) {
        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .cmcTickers(Lists.newArrayListWithCapacity(response.size())).updatedTime(DateTimeUtils.nowUTC()).build();

        for (CoinbaseTickerData item : response) {
            if (item.getPrice() != null && item.getVolume() != null) {
                tickerEntity.getCmcTickers().add(
                    TickerEntity.CMCTicker.builder().baseSymbol(item.getBaseSymbol()).mainSymbol(item.getMainSymbol())
                        .quote(item.getPrice()).mainVolume(item.getPrice().multiply(item.getVolume())).build());
            }
        }
        return tickerEntity;
    }

    @Data
    public static class CoinbaseTickerData {
        private String trade_id;
        private BigDecimal price;
        private BigDecimal size;
        private String time;
        private BigDecimal bid;
        private BigDecimal ask;
        private BigDecimal volume;
        private String baseSymbol;
        private String mainSymbol;
    }
}
