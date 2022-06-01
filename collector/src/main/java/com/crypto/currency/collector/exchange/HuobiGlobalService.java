package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Panzi
 * @Description
 * @date 2022/6/1 22:19
 */
@Exchange(id = "102", name = "Huobi Global")
@Slf4j
public class HuobiGlobalService extends AExchange {

    private final static String URL = "https://api.huobi.pro/market/tickers";

    private static final String OD_URL = "https://api.huobi.pro/market/depth?symbol=";

    private static final String SWAP_URL = "http://api.hbdm.com/swap-ex/market/detail/merged?contract_code=%s";
    private static final String SWAP_INFO_URL = "http://api.hbdm.com/swap-api/v1/swap_contract_info";
    private static final String SWAP_INDEX_URL = "https://api.hbdm.com/swap-api/v1/swap_index";
    private static final String SWAP_BATCH_URL = "https://api.hbdm.com/swap-ex/market/detail/batch_merged";
    private static final String SWAP_INTEREST_URL = "https://api.hbdm.com/swap-api/v1/swap_open_interest";
    private static final String SWAP_FUND_URL = "https://api.hbdm.com/swap-api/v1/swap_funding_rate?contract_code=%s";

    private static final String USDT_SWAP_INFO_URL = "http://api.hbdm.com/linear-swap-api/v1/swap_contract_info";
    private static final String USDT_SWAP_INDEX_URL = "https://api.hbdm.com/linear-swap-api/v1/swap_index";
    private static final String USDT_SWAP_BATCH_URL = "https://api.hbdm.com/linear-swap-ex/market/detail/batch_merged";
    private static final String USDT_SWAP_INTEREST_URL = "https://api.hbdm.com/linear-swap-api/v1/swap_open_interest";
    private static final String USDT_SWAP_FUND_URL =
        "https://api.hbdm.com/linear-swap-api/v1/swap_funding_rate?contract_code=%s";

    private static final String CONTRACT_INFO = "https://api.hbdm.com/api/v1/contract_contract_info";
    private static final String DETAIL_MERGED = "https://api.hbdm.com/market/detail/merged?symbol=%s";
    private static final String OPEN_INTEREST = "https://api.hbdm.com/api/v1/contract_open_interest";
    private static final String TRADE_URL = "https://api.hbdm.com/market/trade?symbol=%s";
    private static final String CONTRACT_INDEX = "https://api.hbdm.com/api/v1/contract_index";

    /**
     * result:{"status":"ok","ts":1590720672526,"data":[{"symbol":"tnbbtc","open":1.992E-7,"high":1.997E-7,"low":1.801E-7,"close":1.885E-7,"amount":1.614288379231258E8,"vol":32.498451376,"count":7554,"bid":1.883E-7,"bidSize":8896.0,"ask":1.899E-7,"askSize":1679.0},{"symbol":"paybtc","open":5.07E-6,"high":5.09E-6,"low":4.91E-6,"close":5.02E-6,"amount":234080.76,"vol":1.1878023029,"count":1573,"bid":5.0E-6,"bidSize":3187.79,"ask":5.04E-6,"askSize":1695.77}]}
     *
     * @return
     */
    @Override
    protected Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {

        return get(URL).map(result -> JacksonUtils.deserialize(result, HuobiGlobalResponse.class))
            .map(r -> toEntity(r));

    }

    private TickerEntity toEntity(HuobiGlobalResponse response) {
        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .cmcTickers(Lists.newArrayListWithCapacity(1)).updatedTime(DateTimeUtils.nowUTC()).build();
        for (HuobiGlobalData data : response.getData()) {
            String symbolPair = StringUtils.upperCase(data.getSymbol());
            Pair<String, String> p = symbolUtils.splitWithFullString(getExchangeId(), symbolPair);
            if (p != null) {
                tickerEntity.getCmcTickers().add(
                    TickerEntity.CMCTicker.builder().baseSymbol(p.getKey()).mainSymbol(p.getValue())
                        .quote(data.getClose()).mainVolume(data.getVol()).build());
            } else {
                tickerEntity.getCmcTickers().add(
                    TickerEntity.CMCTicker.builder().baseSymbol(symbolPair).quote(data.getClose())
                        .mainVolume(data.getVol()).build());
            }
        }
        return tickerEntity;
    }

    @Data
    public static class HuobiGlobalResponse {

        private String status;
        private Long ts;
        private List<HuobiGlobalData> data;

    }

    @Data
    public static class HuobiGlobalData {

        private String symbol;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private BigDecimal amount;
        private BigDecimal vol;
        private Integer count;
        private BigDecimal bid;
        private Integer bidSize;
        private BigDecimal ask;
        private Integer askSize;
    }
}
