package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.collector.util.SymbolUtils;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.ExtUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Panzi
 * @Description
 * @date 2022/6/2 22:29
 * PI Docs : https://docs.bitfinex.com/v1/docs
 * https://docs.bitfinex.com/reference#rest-public-tickers
 * https://docs.bitfinex.com/reference#rest-public-status
 */
@Slf4j
@Exchange(id = "37", name = "Bitfinex")
public class BitfinexService extends AExchange {

    private final static String URL = "https://api-pub.bitfinex.com/v2/tickers?symbols=ALL";
    //所有衍生品status
    private final static String STATUS_URL = "https://api-pub.bitfinex.com/v2/status/deriv?keys=ALL";
    private final static String ORDER_BOOK_URL = "https://api.bitfinex.com/v1/book/%s%s";

    @Override
    public Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {
        return get(URL).map(m -> JacksonUtils.deserialize(m, new TypeReference<List<String[]>>() {
        })).map(item -> this.toEntity(item, "spot"));
    }

    private TickerEntity toEntity(List<String[]> response, String type) {
        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .cmcTickers(Lists.newArrayListWithCapacity(response.size())).updatedTime(DateTimeUtils.nowUTC()).build();

        for (String[] entity : response) {
            String symbol = entity[0];
            if (!StringUtils.startsWith(symbol, "t")) {
                continue;
            }

            symbol = StringUtils.removeStart(symbol, "t");

            Pair<String, String> pair = null;

            if (StringUtils.contains(symbol, ":")) {
                pair = SymbolUtils.splitWithSymbol(symbol, ":");
                if (StringUtils.endsWith(pair.getRight(), "F0") && StringUtils.equalsIgnoreCase(type, "spot")) {
                    // when there is :, its for swap
                    continue;
                } else if (!StringUtils.endsWith(pair.getRight(), "F0") && StringUtils.equalsIgnoreCase(type, "swap")) {
                    // when there is :, its for spot
                    continue;
                }
                pair = Pair.of(StringUtils.removeEnd(pair.getLeft(), "F0"),
                    StringUtils.removeEnd(StringUtils.removeEnd(pair.getRight(), "t0"), "F0"));
            } else {
                if (StringUtils.equalsIgnoreCase(type, "swap")) {
                    // when there is NO :, its for swap
                    continue;
                }
                pair = symbolUtils.splitWithFullString(getExchangeId(), symbol);
            }

            if (pair != null) {
                tickerEntity.getCmcTickers().add(
                    TickerEntity.CMCTicker.builder().mainSymbol(pair.getRight()).baseSymbol(pair.getLeft())
                        .quote(new BigDecimal(entity[7]))
                        .mainVolume(ExtUtils.parseBigDecimal(entity[8]).multiply(ExtUtils.parseBigDecimal((entity[7]))))
                        .build());
            } else {
                tickerEntity.getCmcTickers().add(
                    TickerEntity.CMCTicker.builder().baseSymbol(symbol).quote(new BigDecimal(entity[7]))
                        .mainVolume(ExtUtils.parseBigDecimal(entity[8]).multiply(ExtUtils.parseBigDecimal((entity[7]))))
                        .build());
            }
        }
        return tickerEntity;
    }
}
