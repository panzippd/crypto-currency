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
        //todo deserialize maybe wrong
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

    //    @Override
    //    public Mono<OrderBookEntity> getOrderBooks(ExchangeScheduleTaskEntity task) {
    //        return get(buildProxyUrl(OB_URL + task.getBaseSymbol() + task.getMainSymbol(), null)).map(
    //            m -> this.toOrderBookEntity(task, JSON.parseObject(m, BinanceOrderBookData.class)));
    //    }
    //
    //    private OrderBookEntity toOrderBookEntity(ExchangeScheduleTaskEntity task,
    //        BinanceOrderBookData binanceOrderBookData) {
    //        OrderBookEntity ob =
    //            OrderBookEntity.builder().exchangeId(getExchangeId()).snapshotTime(System.currentTimeMillis())
    //                .baseId(task.getBaseId()).quoteId(task.getMainId()).build();
    //
    //        ob.setAsks(binanceOrderBookData.getAsks());
    //        ob.setBids(binanceOrderBookData.getBids());
    //
    //        return ob;
    //    }

    //    @Override
    //    public Mono<TickerEntity> getFuturesTickers(
    //        ExchangeScheduleTaskEntity task) {  //binance这个组接口由全部Futures数据和少量PERPETUAL数据
    //        return Mono.zip(
    //                get(buildProxyUrl(COIN_FUTURES_TICKER_24HR, null)).map(m -> JSON.parseArray(m, BinanceTickerData.class)),
    //                get(buildProxyUrl(COIN_FUTURES_BOOK_TICKER, null)).map(m -> JSON.parseArray(m, BinanceBookTicker.class)),
    //                get(buildProxyUrl(COIN_FUTURES_PREMIUM_INDEX, null)).map(
    //                    m -> JSON.parseArray(m, BinancePremiumIndex.class)),
    //                get(buildProxyUrl(COIN_FUTURES_ECHANGE_INFO, null)).map(m -> JSON.parseObject(m, BinanceExInfoData.class)))
    //            .map(r -> this.toFuturesEntity(r.getT1(), r.getT2(), r.getT3(), r.getT4()))
    //            .flatMapMany(d -> this.getInterest(d)).collectList().map(
    //                d -> TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
    //                    .derivativesTicker(d).updatedTime(ExtUtils.nowUTC()).build());
    //
    //    }
    //
    //    private List<TickerEntity.DerivativesTicker> toFuturesEntity(List<BinanceTickerData> binanceTickerDatas,
    //        List<BinanceBookTicker> binanceBookTickers, List<BinancePremiumIndex> binancePremiumIndices,
    //        BinanceExInfoData binanceExInfoData) {
    //        List<BinanceExInfoSymbol> binanceExInfoSymbols = binanceExInfoData.getSymbols();
    //        Map<String, BinanceExInfoSymbol> infoSymbolMap = binanceExInfoSymbols.stream()
    //            .collect(Collectors.toMap(BinanceExInfoSymbol::getSymbol, Function.identity(), (key1, key2) -> key2));
    //        List<TickerEntity.DerivativesTicker> derivativesTickers =
    //            Lists.newArrayListWithCapacity(binanceTickerDatas.size());
    //        Map<String, BinanceBookTicker> binanceBookTickerMap = binanceBookTickers.stream()
    //            .collect(Collectors.toMap(i -> i.getSymbol(), Function.identity(), (key1, key2) -> key2));
    //        Map<String, BinancePremiumIndex> binancePremiumIndexMap = binancePremiumIndices.stream()
    //            .collect(Collectors.toMap(i -> i.getSymbol(), Function.identity(), (key1, key2) -> key2));
    //        for (BinanceTickerData binanceTickerData : binanceTickerDatas) {
    //            String symbol = binanceTickerData.getSymbol();
    //            BinanceExInfoSymbol exchangeInfo = infoSymbolMap.get(symbol);
    //            if (null == exchangeInfo || null == binanceBookTickerMap.get(symbol) || null == binancePremiumIndexMap.get(
    //                symbol)) {   //数据不完整的交易对去除
    //                continue;
    //            }
    //            String productType = EDataType.PERPETUAL.getCategory();
    //            String deliveryTime = EDataType.PERPETUAL.getCategory();
    //
    //            if (!"PERPETUAL".equals(exchangeInfo.getContractType())) {
    //                productType = EDataType.FUTURES.getCategory();
    //                deliveryTime = DeliveryTimeType.QUARTER.getCategory();  //这个接口 只有季度合约
    //
    //            }
    //            BigDecimal contractSize =
    //                exchangeInfo.getContractSize();//期货一张交易单的容量,单位美元?,openInterestUSD的计算需要这个字段,存在openInterest带过去
    //            TickerEntity.DerivativesTicker ticker =
    //                TickerEntity.DerivativesTicker.builder().productType(productType).tickerId(symbol)
    //                    .deliveryTime(deliveryTime).baseSymbol(exchangeInfo.getBaseAsset())
    //                    .mainSymbol(exchangeInfo.getQuoteAsset())
    //                    .creationTimestamp(ExtUtils.toLocalDateTime(exchangeInfo.getOnboardDate()))
    //                    .expiryTimestamp(ExtUtils.toLocalDateTime(exchangeInfo.getDeliveryDate()))
    //                    .quote(binanceTickerData.getLastPrice())
    //                    .baseVolume(ExtUtils.getNotNull(binanceTickerData.getBaseVolume()))
    //                    .usdVolume(ExtUtils.getNotNull(binanceTickerData.getVolume().multiply(contractSize)))
    //                    .mainVolume(ExtUtils.getNotNull(binanceTickerData.getVolume().multiply(contractSize)))
    //                    .fundingRate(ExtUtils.getNotNull(binancePremiumIndexMap.get(symbol).getLastFundingRate()))
    //                    .bid(ExtUtils.getNotNull(binanceBookTickerMap.get(symbol).getBidPrice()))
    //                    .ask(ExtUtils.getNotNull(binanceBookTickerMap.get(symbol).getAskPrice()))
    //                    .high(ExtUtils.getNotNull(binanceTickerData.getHighPrice())).openInterest(contractSize)
    //                    .low(ExtUtils.getNotNull(binanceTickerData.getLowPrice()))
    //                    .indexPrice(ExtUtils.getNotNull(binancePremiumIndexMap.get(symbol).getMarkPrice())).build();
    //            derivativesTickers.add(ticker);
    //
    //        }
    //        return derivativesTickers;
    //    }
    //
    //    //    todo 新perps数据接口,数据饿老perps数据有差异.本次先增加全新的futures数据接口
    //    @Override
    //    public Mono<TickerEntity> getPerpetualTickers(ExchangeScheduleTaskEntity task) {
    //        return Mono.zip(get(buildProxyUrl(SWAP_URL, null)).map(m -> JSON.parseArray(m, BinanceTickerData.class)),
    //                get(buildProxyUrl(BOOK_TICKER, null)).map(m -> JSON.parseArray(m, BinanceBookTicker.class)),
    //                get(buildProxyUrl(PERMIUM_INDEX, null)).map(m -> JSON.parseArray(m, BinancePremiumIndex.class)),
    //                get(buildProxyUrl(SWAP_INFO, null)).map(m -> JSON.parseObject(m, BinanceExInfoData.class)))
    //            .map(r -> this.toPerpetualEntity(r.getT1(), r.getT2(), r.getT3(), r.getT4()))
    //            .flatMapMany(this::getPerpetualInterest).collectList().map(
    //                d -> TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
    //                    .derivativesTicker(d).updatedTime(ExtUtils.nowUTC()).build());
    //
    //    }
    //
    //    private List<TickerEntity.DerivativesTicker> toPerpetualEntity(List<BinanceTickerData> binanceTickerDatas,
    //        List<BinanceBookTicker> binanceBookTickers, List<BinancePremiumIndex> binancePremiumIndices,
    //        BinanceExInfoData binanceExInfoData) {
    //        List<BinanceExInfoSymbol> binanceExInfoSymbols = binanceExInfoData.getSymbols();
    //        Map<String, BinanceExInfoSymbol> infoSymbolMap = binanceExInfoSymbols.stream()
    //            .collect(Collectors.toMap(BinanceExInfoSymbol::getSymbol, Function.identity(), (key1, key2) -> key2));
    //        List<TickerEntity.DerivativesTicker> derivativesTickers =
    //            Lists.newArrayListWithCapacity(binanceTickerDatas.size());
    //        Map<String, BinanceBookTicker> binanceBookTickerMap = binanceBookTickers.stream()
    //            .collect(Collectors.toMap(i -> i.getSymbol(), Function.identity(), (key1, key2) -> key2));
    //        Map<String, BinancePremiumIndex> binancePremiumIndexMap = binancePremiumIndices.stream()
    //            .collect(Collectors.toMap(i -> i.getSymbol(), Function.identity(), (key1, key2) -> key2));
    //        for (BinanceTickerData binanceTickerData : binanceTickerDatas) {
    //            String symbol = binanceTickerData.getSymbol();
    //            BinanceExInfoSymbol exchangeInfo = infoSymbolMap.get(symbol);
    //            if (null == exchangeInfo || null == binanceBookTickerMap.get(symbol) || null == binancePremiumIndexMap.get(
    //                symbol)) {
    //                continue;
    //            }
    //            String productType = EDataType.PERPETUAL.getCategory();
    //            String deliveryTime = EDataType.PERPETUAL.getCategory();
    //
    //            if (!"PERPETUAL".equals(exchangeInfo.getContractType())) {
    //                productType = EDataType.FUTURES.getCategory();
    //                deliveryTime = DeliveryTimeType.MONTH.getCategory();  //这个接口 可能有月度合约
    //
    //            }
    //
    //            //这个接口有80个永续合约,和2个期货(BTCBUSD 即将下架)
    //            TickerEntity.DerivativesTicker ticker =
    //                TickerEntity.DerivativesTicker.builder().productType(productType).tickerId(symbol)
    //                    .baseSymbol(exchangeInfo.getBaseAsset()).mainSymbol(exchangeInfo.getQuoteAsset())
    //                    .creationTimestamp(ExtUtils.toLocalDateTime(exchangeInfo.getOnboardDate()))
    //                    .expiryTimestamp(ExtUtils.toLocalDateTime(exchangeInfo.getDeliveryDate()))
    //                    .deliveryTime(deliveryTime).quote(binanceTickerData.getLastPrice())
    //                    .baseVolume(ExtUtils.getNotNull(binanceTickerData.getVolume()))
    //                    .usdVolume(ExtUtils.getNotNull(binanceTickerData.getQuoteVolume()))
    //                    .mainVolume(ExtUtils.getNotNull(binanceTickerData.getQuoteVolume()))
    //                    .fundingRate(ExtUtils.getNotNull(binancePremiumIndexMap.get(symbol).getLastFundingRate()))
    //                    .bid(ExtUtils.getNotNull(binanceBookTickerMap.get(symbol).getBidPrice()))
    //                    .ask(ExtUtils.getNotNull(binanceBookTickerMap.get(symbol).getAskPrice()))
    //                    .high(ExtUtils.getNotNull(binanceTickerData.getHighPrice()))
    //                    .low(ExtUtils.getNotNull(binanceTickerData.getLowPrice()))
    //                    .indexPrice(ExtUtils.getNotNull(binancePremiumIndexMap.get(symbol).getMarkPrice())).build();
    //            derivativesTickers.add(ticker);
    //
    //        }
    //        return derivativesTickers;
    //    }
    //
    //    private Flux<TickerEntity.DerivativesTicker> getInterest(List<TickerEntity.DerivativesTicker> derivativesTickers) {
    //        return Flux.fromIterable(derivativesTickers).flatMap(
    //            d -> get(String.format(COIN_FUTURES_OPEN_INTEREST, d.getTickerId())).map(
    //                m -> JSON.parseObject(m, BinanceOpenInterest.class)).map(b -> this.interestEntity(d, b)));
    //    }
    //
    //    private TickerEntity.DerivativesTicker interestEntity(TickerEntity.DerivativesTicker derivativesTicker,
    //        BinanceOpenInterest binanceOpenInterest) {
    //        derivativesTicker.setOpen_InterestUsd(binanceOpenInterest.getOpenInterest()
    //            .multiply(derivativesTicker.getOpenInterest()));     //接口返回的是未平仓合约数,计算Open_InterestUsd可能需要乘以每张的币值:100usd
    //        derivativesTicker.setOpenInterest(binanceOpenInterest.getOpenInterest());
    //        return derivativesTicker;
    //    }
    //
    //    private Flux<TickerEntity.DerivativesTicker> getPerpetualInterest(
    //        List<TickerEntity.DerivativesTicker> derivativesTickers) {
    //        return Flux.fromIterable(derivativesTickers).flatMap(
    //            d -> get(String.format(OPEN_INTEREST, d.getTickerId())).map(
    //                m -> JSON.parseObject(m, BinanceOpenInterest.class)).map(b -> this.perpetualInterestEntity(d, b)));
    //    }
    //
    //    private TickerEntity.DerivativesTicker perpetualInterestEntity(TickerEntity.DerivativesTicker derivativesTicker,
    //        BinanceOpenInterest binanceOpenInterest) {
    //        derivativesTicker.setOpen_InterestUsd(binanceOpenInterest.getOpenInterest());
    //        return derivativesTicker;
    //    }

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
