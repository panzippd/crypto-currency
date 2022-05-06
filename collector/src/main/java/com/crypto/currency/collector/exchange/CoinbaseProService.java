package com.crypto.currency.collector.exchange;

import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * @author Panzi
 * @Description the exchange Coinbase
 * @date 2022/5/6 23:28
 */
public class CoinbaseProService {

    private final static String URL = "https://discover.coinmarketcap.supply/exchange/89";

    private static final String OD_URL = "https://api.pro.coinbase.com/products/";

    private final static String PAIRS_URL = "https://api.pro.coinbase.com/products/";

    private final static String TICKER_URL = "https://api.pro.coinbase.com/products/%s/ticker";

    //    @Override
    protected Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {
        //        return get(PAIRS_URL).map(r -> JacksonUtils.readTree(r)).flatMapIterable(this::getPairList)
        //            .flatMap(item -> get(buildProxyUrl(String.format(TICKER_URL, item), null)).map(m -> {
        //                CoinbaseTickerData coinbaseTickerData = JSON.parseObject(m, CoinbaseTickerData.class);
        //                Pair<String, String> pair = SymbolUtils.splitWithDash(item);
        //                coinbaseTickerData.setBaseSymbol(pair.getLeft());
        //                coinbaseTickerData.setMainSymbol(pair.getRight());
        //                return coinbaseTickerData;
        //            })).collectList().map(this::toEntity);
        return null;
    }

    //    private List<String> getPairList(JsonNode jsonNode) {
    //        var jsons = jsonNode.elements();
    //        if (jsons == null) {
    //            BusinessException.throwIfErrorCode(MessageCode.SYS_BODY_NULL);
    //        }
    //        List<String> pairList = new ArrayList<>();
    //        while (jsons.hasNext()) {
    //            JsonNode son = jsons.next();
    //            pairList.add(son.get("id").textValue());
    //        }
    //        return pairList;
    //    }
    //
    //    private TickerEntity toEntity(List<CoinbaseTickerData> response) {
    //        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
    //            .cmcTickers(Lists.newArrayListWithCapacity(response.size())).updatedTime(ExtUtils.nowUTC()).build();
    //
    //        for (CoinbaseTickerData item : response) {
    //            if (item.getPrice() != null && item.getVolume() != null) {
    //                tickerEntity.getCmcTickers().add(
    //                    TickerEntity.CMCTicker.builder().baseSymbol(item.getBaseSymbol()).mainSymbol(item.getMainSymbol())
    //                        .quote(item.getPrice()).mainVolume(item.getPrice().multiply(item.getVolume())).build());
    //            }
    //        }
    //        return tickerEntity;
    //    }

    /**
     * 文档:https://docs.pro.coinbase.com/#get-product-order-book
     * https://api.pro.coinbase.com/products/BTC-USD/book?level=2
     * 结果:{"sequence":14514643295,"bids":[["9540.86","3.10149878",3],["9537.02","0.001",1],["9537","0.886",2],["9536.06","1.5",1],["9535.02","0.5",1],["9534.98","3.999",1],["9534.32","0.503938",1],["9534.31","0.34478246",1],["9534.3","0.42060787",1],["9533.56","0.4",1],["9533.54","0.2922",1]]}
     */
    //    @Override
    //    protected Mono<OrderBookEntity> getOrderBooks(ScheduleTaskEntity task) {
    //        return get(OD_URL + task.getBaseSymbol() + "-" + task.getMainSymbol() + "/book?level=2").map(
    //            m -> this.toOrderBookEntity(task, JSON.parseObject(m, CoinbaseProOrderBookData.class)));
    //    }
    //
    //    private OrderBookEntity toOrderBookEntity(ScheduleTaskEntity task, CoinbaseProOrderBookData data) {
    //        OrderBookEntity ob =
    //            OrderBookEntity.builder().exchangeId(getExchangeId()).snapshotTime(System.currentTimeMillis())
    //                .baseId(task.getBaseId()).quoteId(task.getMainId()).build();
    //
    //        ob.setAsks(getAskBid(data.getAsks()));
    //        ob.setBids(getAskBid(data.getBids()));
    //
    //        return ob;
    //    }
    //
    //    private List<BigDecimal[]> getAskBid(List<BigDecimal[]> i) {
    //        List<BigDecimal[]> r = Lists.newArrayListWithCapacity(i.size());
    //
    //        for (var item : i) {
    //            BigDecimal[] b = new BigDecimal[2];
    //            b[0] = item[0];
    //            b[1] = item[1];
    //            r.add(b);
    //        }
    //        return r;
    //    }

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
