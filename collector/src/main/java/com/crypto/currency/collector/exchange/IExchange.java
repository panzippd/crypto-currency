package com.crypto.currency.collector.exchange;

import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/15 21:48
 */
public interface IExchange {

    /**
     * get ticker data
     *
     * @param taskEntity
     * @return
     */
    Mono<TickerEntity> getTickerData(ExchangeScheduleTaskEntity taskEntity);

    /**
     * get order books
     *
     * @param taskEntity
     * @return
     */
    //    Mono<OrderBookEntity> getOrderBooksData(ExchangeScheduleTaskEntity taskEntity);

    /**
     * get spot data
     *
     * @param taskEntity
     * @return
     */
    Mono<TickerEntity> getSwapData(final ExchangeScheduleTaskEntity taskEntity);

    /**
     * get options data
     *
     * @param taskEntity
     * @return
     */
    Mono<TickerEntity> getOptionsData(final ExchangeScheduleTaskEntity taskEntity);

    /**
     * get perpetual data
     *
     * @param taskEntity
     * @return
     */
    Mono<TickerEntity> getPerpetualData(final ExchangeScheduleTaskEntity taskEntity);

    /**
     * get futures data
     *
     * @param taskEntity
     * @return
     */
    Mono<TickerEntity> getFuturesData(final ExchangeScheduleTaskEntity taskEntity);

    /**
     * get exchange TradingPairs
     *
     * @param taskEntity
     * @return
     */
    default Flux<ExchangeScheduleTaskEntity> getTradingPairDatas(ExchangeScheduleTaskEntity taskEntity) {
        return null;
    }

}
