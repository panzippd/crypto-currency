package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.common.exception.BusinessException;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.ExtUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * @author Panzi
 * @Description
 * @date 2022/6/5 23:16
 */
@Slf4j
@Exchange(id = "630", name = "Binance.US")
public class BinanceUsService extends AExchange {
    private static final String URL = "https://api.binance.us/api/v1/ticker/24hr";
    private static final String ORDER_BOOK_URL = "https://api.binance.us/api/v3/depth?symbol=%s%s&limit=100";

    @Override
    protected Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {
        return get(URL).map(r -> JacksonUtils.readTree(r)).map(this::toEntity);
    }

    /**
     * toEntity
     *
     * @param jsonNode
     * @return
     */
    private TickerEntity toEntity(JsonNode jsonNode) {

        var jsons = jsonNode.elements();
        if (jsons == null) {
            BusinessException.throwIfMessage("the result is null");
        }
        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .updatedTime(DateTimeUtils.nowUTC()).build();
        tickerEntity.setCmcTickers(Lists.newArrayListWithCapacity(jsonNode.size()));
        while (jsons.hasNext()) {
            JsonNode son = jsons.next();
            if (son == null) {
                continue;
            }
            JsonNode symbolNode = son.get("symbol");
            if (symbolNode == null) {
                continue;
            }
            String name = symbolNode.textValue();
            if (StringUtils.isBlank(name)) {
                continue;
            }
            name = name.toUpperCase();
            Pair<String, String> symbolPair = symbolUtils.splitWithFullString(getExchangeId(), name);
            BigDecimal quote = ExtUtils.parseBigDecimal(son.get("lastPrice").asText());
            BigDecimal mainVolume = ExtUtils.parseBigDecimal(son.get("quoteVolume").asText());
            if (symbolPair != null) {
                tickerEntity.getCmcTickers().add(
                    TickerEntity.CMCTicker.builder().baseSymbol(symbolPair.getLeft()).mainSymbol(symbolPair.getRight())
                        .quote(quote).mainVolume(mainVolume).build());
            } else {
                tickerEntity.getCmcTickers()
                    .add(TickerEntity.CMCTicker.builder().baseSymbol(name).quote(quote).mainVolume(mainVolume).build());
            }
        }
        return tickerEntity;
    }
}
