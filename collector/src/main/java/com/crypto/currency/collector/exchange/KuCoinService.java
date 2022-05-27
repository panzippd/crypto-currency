package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.collector.util.SymbolUtils;
import com.crypto.currency.common.exception.BusinessException;
import com.crypto.currency.common.utils.CollectionUtils;
import com.crypto.currency.common.utils.ExtUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.TickerEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Iterator;
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
    public Mono<TickerEntity> getTickers(ScheduleTaskEntity taskEntity) {
        return get(URL).map(m -> JSON.parseObject(m, KuCoinTickerData.class)).map(this::toEntity);
    }

    private TickerEntity toEntity(KuCoinTickerData response) {
        List<KCTicker> tickers = response.getData().getTicker();

        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .cmcTickers(Lists.newArrayListWithCapacity(tickers.size())).updatedTime(ExtUtils.nowUTC()).build();

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

    @Override
    protected Mono<OrderBookEntity> getOrderBooks(final ScheduleTaskEntity taskEntity) {

        if (isFullQuery(taskEntity)) {
            log.info("should not full query");
            return Mono.empty();
        }
        final String mainSymbol = StringUtils.upperCase(taskEntity.getMainSymbol());
        final String baseSymbol = StringUtils.upperCase(taskEntity.getBaseSymbol());
        String url = String.format(ORDER_BOOK_URL, baseSymbol, mainSymbol);
        return get(url).map(r -> JacksonUtils.readTree(r)).map(r -> toOrderBookEntity(r, taskEntity));
    }

    /**
     * toOrderBookEntity
     *
     * @param jsonNode
     * @param taskEntity
     * @return
     */
    private OrderBookEntity toOrderBookEntity(JsonNode jsonNode, final ScheduleTaskEntity taskEntity) {

        JsonNode orderBooks = jsonNode.at("/data");
        if (orderBooks == null) {
            BusinessException.throwIfErrorCode(MessageCode.SYS_BODY_NULL);
        }
        OrderBookEntity ob =
            OrderBookEntity.builder().exchangeId(getExchangeId()).snapshotTime(System.currentTimeMillis())
                .baseId(taskEntity.getBaseId()).quoteId(taskEntity.getMainId()).build();
        JsonNode ask = orderBooks.get("asks");
        JsonNode bid = orderBooks.get("bids");
        Iterator<JsonNode> asks = ask.elements();
        Iterator<JsonNode> bids = bid.elements();
        List<BigDecimal[]> price_quantity = getTrades(asks, asks != null ? ask.size() : 8);
        if (CollectionUtils.isNotEmpty(price_quantity)) {
            ob.setAsks(price_quantity);
        }
        price_quantity = getTrades(bids, bids != null ? bid.size() : 8);
        if (CollectionUtils.isNotEmpty(price_quantity)) {
            ob.setBids(price_quantity);
        }
        return ob;
    }

    /**
     * getTrades
     *
     * @param ask
     * @return
     */
    private List<BigDecimal[]> getTrades(Iterator<JsonNode> ask, int size) {

        if (ask == null) {
            return null;
        }
        List<BigDecimal[]> orderBooks = Lists.newArrayListWithCapacity(size);
        while (ask.hasNext()) {
            JsonNode nodes = ask.next();
            if (nodes == null) {
                continue;
            }
            Iterator<JsonNode> prices = nodes.elements();
            if (prices == null) {
                continue;
            }
            List<JsonNode> price_quantity = IteratorUtils.toList(prices, 2);
            if (CollectionUtils.size(price_quantity) >= 2) {
                orderBooks.add(new BigDecimal[] {ExtUtils.parseBigDecimal(price_quantity.get(0).asText()),
                    ExtUtils.parseBigDecimal((price_quantity.get(1).asText()))});
            }
        }
        return orderBooks;
    }

    @Override
    public Mono<TickerEntity> getPerpetualTickers(ScheduleTaskEntity taskEntity) {
        return get(FUTURE_URL).map(
                m -> JSON.parseArray(JacksonUtils.readTree(m).at("/data").toString(), KuCoinDerivTickerData.class))
            .map(this::toPerpetualEntity).flatMapIterable(r -> r).flatMap(
                d -> get(String.format(FUNDING_RATE_URL, d.getTickerId())).map(
                        m -> JSON.parseObject(JacksonUtils.readTree(m).at("/data").toString(), KuCoinFundingRate.class))
                    .map(b -> this.tickerEntitySetFundingRate(d, b)).flatMap(
                        r -> get(String.format(FUTURE_TICKER_URL, r.getTickerId())).map(
                                m -> JSON.parseObject(JacksonUtils.readTree(m).at("/data").toString(), KuCoinAskBid.class))
                            .map(b -> this.tickerEntitySetAskBid(d, b)))).collectList().map(
                d -> TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
                    .derivativesTicker(d).updatedTime(ExtUtils.nowUTC()).build());
    }

    private List<TickerEntity.DerivativesTicker> toPerpetualEntity(List<KuCoinDerivTickerData> kuCoinDerivTickerDatas) {
        if (com.cmc.worker.common.utils.CollectionUtils.isEmpty(kuCoinDerivTickerDatas)) {
            BusinessException.throwIfErrorCode(MessageCode.SYS_BODY_NULL);
        }
        List<TickerEntity.DerivativesTicker> derivativesTickers =
            Lists.newArrayListWithExpectedSize(kuCoinDerivTickerDatas.size());
        for (KuCoinDerivTickerData kuCoinDerivTickerData : kuCoinDerivTickerDatas) {
            if ("FFWCSX".equals(kuCoinDerivTickerData.getType().name())) {
                String productType = EDataType.PERPETUAL.getCategory();
                String deliveryTime = EDataType.PERPETUAL.getCategory();
                BigDecimal quoteVolume = kuCoinDerivTickerData.getVolumeOf24h();
                if ("USDT".equals(kuCoinDerivTickerData.getQuote())) {
                    quoteVolume = kuCoinDerivTickerData.getTurnoverOf24h();
                }
                BigDecimal openInterest = kuCoinDerivTickerData.getOpenInterest();
                if ("USDT".equals(kuCoinDerivTickerData.getQuote())) {
                    openInterest = ExtUtils.getNotNull(kuCoinDerivTickerData.getOpenInterest()).multiply(
                        ExtUtils.getNotNull(kuCoinDerivTickerData.getMultiplier())
                            .multiply(kuCoinDerivTickerData.getLast()));
                }
                TickerEntity.DerivativesTicker ticker =
                    TickerEntity.DerivativesTicker.builder().productType(productType)
                        .tickerId(kuCoinDerivTickerData.getSymbol()).deliveryTime(deliveryTime)
                        .baseSymbol(kuCoinDerivTickerData.getBase()).mainSymbol(kuCoinDerivTickerData.getQuote())
                        .expiryTimestamp(ExtUtils.toLocalDateTime(DataConstants.TWENTY_SECOND_CENTURY))
                        .quote(kuCoinDerivTickerData.getLast()).usdVolume(ExtUtils.getNotNull(quoteVolume))
                        .open_InterestUsd(openInterest).high(ExtUtils.getNotNull(kuCoinDerivTickerData.getHighPrice()))
                        .makerFee(kuCoinDerivTickerData.getMakerFeeRate())
                        .takerFee(kuCoinDerivTickerData.getTakerFeeRate())
                        .low(ExtUtils.getNotNull(kuCoinDerivTickerData.getLowPrice()))
                        .indexPrice(ExtUtils.getNotNull(kuCoinDerivTickerData.getIndexPrice())).build();
                derivativesTickers.add(ticker);
            }
        }
        return derivativesTickers;
    }

    private TickerEntity.DerivativesTicker tickerEntitySetFundingRate(TickerEntity.DerivativesTicker derivativesTicker,
        KuCoinFundingRate kuCoinFundingRate) {
        derivativesTicker.setFundingRate(kuCoinFundingRate.getValue());
        return derivativesTicker;
    }

    @Override
    public Mono<TickerEntity> getFuturesTickers(ScheduleTaskEntity taskEntity) {
        return get(FUTURE_URL).map(
                m -> JSON.parseArray(JacksonUtils.readTree(m).at("/data").toString(), KuCoinDerivTickerData.class))
            .map(this::toFuturesEntity).flatMapIterable(r -> r).flatMap(
                d -> get(String.format(FUTURE_TICKER_URL, d.getTickerId())).map(
                        m -> JSON.parseObject(JacksonUtils.readTree(m).at("/data").toString(), KuCoinAskBid.class))
                    .map(b -> this.tickerEntitySetAskBid(d, b))).collectList().map(
                d -> TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
                    .derivativesTicker(d).updatedTime(ExtUtils.nowUTC()).build());
    }

    private List<TickerEntity.DerivativesTicker> toFuturesEntity(List<KuCoinDerivTickerData> kuCoinDerivTickerDatas) {
        if (com.cmc.worker.common.utils.CollectionUtils.isEmpty(kuCoinDerivTickerDatas)) {
            BusinessException.throwIfErrorCode(MessageCode.SYS_BODY_NULL);
        }
        List<TickerEntity.DerivativesTicker> derivativesTickers =
            Lists.newArrayListWithExpectedSize(kuCoinDerivTickerDatas.size());
        for (KuCoinDerivTickerData kuCoinDerivTickerData : kuCoinDerivTickerDatas) {
            if ("FFICSX".equals(kuCoinDerivTickerData.getType().name())) {
                String symbol = kuCoinDerivTickerData.getSymbol();
                String productType = EDataType.FUTURES.getCategory();
                String deliveryTime = DeliveryTimeType.QUARTER.getCategory();
                BigDecimal quoteVolume = kuCoinDerivTickerData.getVolumeOf24h();
                if ("USDT".equals(kuCoinDerivTickerData.getQuote())) {
                    quoteVolume = kuCoinDerivTickerData.getTurnoverOf24h();
                }
                BigDecimal openInterest = kuCoinDerivTickerData.getOpenInterest();
                if ("USDT".equals(kuCoinDerivTickerData.getQuote())) {
                    openInterest = ExtUtils.getNotNull(kuCoinDerivTickerData.getOpenInterest()).multiply(
                        ExtUtils.getNotNull(kuCoinDerivTickerData.getMultiplier())
                            .multiply(kuCoinDerivTickerData.getLast()));
                }
                TickerEntity.DerivativesTicker ticker =
                    TickerEntity.DerivativesTicker.builder().productType(productType).tickerId(symbol)
                        .deliveryTime(deliveryTime).baseSymbol(kuCoinDerivTickerData.getBase())
                        .mainSymbol(kuCoinDerivTickerData.getQuote())
                        .creationTimestamp(ExtUtils.toLocalDateTime(kuCoinDerivTickerData.getOpenTime()))
                        .expiryTimestamp(ExtUtils.toLocalDateTime(kuCoinDerivTickerData.getCloseTime()))
                        .quote(kuCoinDerivTickerData.getLast()).usdVolume(ExtUtils.getNotNull(quoteVolume))
                        .open_InterestUsd(openInterest).high(ExtUtils.getNotNull(kuCoinDerivTickerData.getHighPrice()))
                        .makerFee(kuCoinDerivTickerData.getMakerFeeRate())
                        .takerFee(kuCoinDerivTickerData.getTakerFeeRate())
                        .low(ExtUtils.getNotNull(kuCoinDerivTickerData.getLowPrice()))
                        .indexPrice(ExtUtils.getNotNull(kuCoinDerivTickerData.getIndexPrice())).build();
                derivativesTickers.add(ticker);
            }
        }
        return derivativesTickers;
    }

    private TickerEntity.DerivativesTicker tickerEntitySetAskBid(TickerEntity.DerivativesTicker derivativesTicker,
        KuCoinAskBid kuCoinAskBid) {
        derivativesTicker.setAsk(kuCoinAskBid.getBestAskPrice());
        derivativesTicker.setBid(kuCoinAskBid.getBestBidPrice());
        return derivativesTicker;
    }
}
