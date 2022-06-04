package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Panzi
 * @Description
 * @date 2022/6/4 22:31
 * https://docs.gemini.com/rest-api/#candles
 * https://docs.gemini.com/rest-api/#ticker-v2
 * https://docs.gemini.com/rest-api/#ticker
 * https://api.gemini.com/v1/book/BTCUSD
 * https://docs.gemini.com/rest-api/#symbols
 */
@Slf4j
@Exchange(id = "151", name = "Gemini")
public class GeminiService extends AExchange {
    private static final String URL = "https://api.gemini.com/v1/pubticker/%s%s";
    private static final String OD_URL = "https://api.gemini.com/v1/book/";

    private static final String SYMBOL_URL = "https://api.gemini.com/v1/symbols";

    @Override
    protected Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {

        String url = String.format(URL, StringUtils.lowerCase(taskEntity.getBaseSymbol()),
            StringUtils.lowerCase(taskEntity.getMainSymbol()));
        return get(buildProxyUrl(url, null)).map(m -> JacksonUtils.deserialize(m, GeminiTickerData.class))
            .map(m -> this.toEntity(m, taskEntity));
    }

    //todo maybe can not work
    @Override
    protected Mono<List<ExchangeScheduleTaskEntity>> getTradingPairs(ExchangeScheduleTaskEntity taskEntity) {

        return get(buildProxyUrl(SYMBOL_URL, null)).map(
            r -> JacksonUtils.deserialize(r, new TypeReference<List<String>>() {
            })).flatMapMany(e -> Flux.fromIterable(e)).map(r -> {
            Pair<String, String> pair = symbolUtils.splitWithFullString(getExchangeId(), r.toUpperCase());
            if (pair == null) {
                return Pair.of(r.toUpperCase(), "");
            }
            return pair;
        }).filter(r -> r != null).map(res -> {
            ExchangeScheduleTaskEntity entity =
                ExchangeScheduleTaskEntity.builder().mainId(taskEntity.getMainId()).baseId(taskEntity.getBaseId())
                    .exchangeName(taskEntity.getExchangeName()).exchangeId(taskEntity.getExchangeId())
                    .type(taskEntity.getType()).takeTradingPair(taskEntity.isTakeTradingPair())
                    .baseSymbol(res.getLeft()).build();
            if (StringUtils.isNotBlank(res.getRight())) {
                entity.setMainSymbol(res.getRight());
            }
            return entity;
        }).collectList();
    }

    private TickerEntity toEntity(GeminiTickerData response, ExchangeScheduleTaskEntity taskEntity) {

        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .cmcTickers(Lists.newArrayListWithCapacity(1)).updatedTime(DateTimeUtils.nowUTC()).build();

        BigDecimal mainVol;
        if (response.getVolume() == null || (mainVol = response.getVolume().get(taskEntity.getMainSymbol())) == null) {
            mainVol = BigDecimal.ZERO;
        }

        tickerEntity.getCmcTickers().add(
            TickerEntity.CMCTicker.builder().baseSymbol(taskEntity.getBaseSymbol().toUpperCase())
                .mainSymbol(taskEntity.getMainSymbol().toUpperCase()).quote(response.getLast()).mainVolume(mainVol)
                .build());

        return tickerEntity;
    }

    @Data
    public static class GeminiTickerData {

        private BigDecimal last;
        private BigDecimal bid;
        private BigDecimal ask;
        private Map<String, BigDecimal> volume;
    }
}
