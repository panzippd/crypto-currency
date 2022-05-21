package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.collector.util.SymbolUtils;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/21 21:44
 */
@Exchange(id = "524", name = "FTX")
public class FTXService extends AExchange {

    private final static String URL = "https://ftx.com/api/markets";

    private final static String FUTURES_URL = "https://ftx.com/api/futures";

    private final static String STATS_URL = "https://ftx.com/api/futures/%s/stats";
    //永续合约这个字段才有值
    private final static String FUNDING_RATES = "https://ftx.com/api/funding_rates?start_time=%s";

    /**
     * 结果:{"result":[{"ask":0.076925,"baseCurrency":null,"bid":0.07686,"change1h":-0.025465602432535157,"change24h":-0.06332196785192401,"changeBod":0.03763658437879401,"enabled":true,"last":0.07692,"minProvideSize":1.0,"name":"ADA-PERP","price":0.07692,"priceIncrement":5e-06,"quoteCurrency":null,"quoteVolume24h":20035610.321945,"restricted":false,"sizeIncrement":1.0,"type":"future","underlying":"ADA","volumeUsd24h":20035610.321945},{"ask":0.07736,"baseCurrency":null,"bid":0.07729,"change1h":-0.025201612903225805,"change24h":-0.05910970566772075,"changeBod":0.038389261744966444,"enabled":true,"last":0.077715,"minProvideSize":1.0,"name":"ADA-0626","price":0.07736,"priceIncrement":5e-06,"quoteCurrency":null,"quoteVolume24h":987168.436825,"restricted":false,"sizeIncrement":1.0,"type":"future","underlying":"ADA","volumeUsd24h":987168.436825}]}
     *
     * @return
     */
    @Override
    protected Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity taskEntity) {
        return get(URL).map(result -> JacksonUtils.deserialize(result, FTXResponse.class))
            .map(r -> toEntity(r, "spot"));
    }

    private TickerEntity toEntity(FTXResponse response, String type) {
        TickerEntity tickerEntity = TickerEntity.builder().exchangeId(getExchangeId()).exchangeName(getExchangeName())
            .cmcTickers(Lists.newArrayListWithCapacity(1)).updatedTime(DateTimeUtils.nowUTC()).build();

        String splitor = null;
        if (StringUtils.equalsIgnoreCase(type, "spot")) {
            splitor = "/";
        } else if (StringUtils.equalsIgnoreCase(type, "future")) {
            splitor = "-";
        } else {
            return null;
        }

        for (FTXData data : response.getResult()) {
            if (StringUtils.equalsIgnoreCase(type, data.getType())) {
                if (StringUtils.equals(type, "future") && !StringUtils.endsWith(data.getName(), "-PERP")) {
                    // only collect perp/swap
                    continue;
                }
                Pair<String, String> p = SymbolUtils.splitWithSymbol(data.getName(), splitor);
                if (p == null) {
                    continue;
                }

                String base = p.getLeft();
                String main = p.getRight();

                BigDecimal volume;
                if (StringUtils.equalsIgnoreCase(data.getType(), "spot")) {
                    volume = data.getQuoteVolume24h();
                } else if (StringUtils.equalsIgnoreCase(data.getType(), "future")) {
                    volume = data.getVolumeUsd24h();
                } else {
                    volume = BigDecimal.ZERO;
                }

                tickerEntity.getCmcTickers().add(
                    TickerEntity.CMCTicker.builder().baseSymbol(base).mainSymbol(main).quote(data.getPrice())
                        .mainVolume(volume).build());
            }
        }
        return tickerEntity;
    }

    @Data
    public static class FTXResponse {

        private List<FTXData> result;

    }

    @Data
    public static class FTXData {

        private BigDecimal ask;
        private BigDecimal bid;
        private BigDecimal change1h;
        private BigDecimal change24h;
        private BigDecimal changeBod;
        private String description;
        private Boolean enabled;
        private Boolean expired;
        private Date expiry;
        private String expiryDescription;
        private String group;
        private BigDecimal imfFactor;
        private BigDecimal index;
        private BigDecimal last;
        private BigDecimal lowerBound;
        private BigDecimal marginPrice;
        private BigDecimal mark;
        private Date moveStart;
        private BigDecimal minProvideSize;
        private String name;
        private Boolean perpetual;
        private Integer positionLimitWeight;
        private Boolean postOnly;

        private BigDecimal price;
        private BigDecimal priceIncrement;
        private BigDecimal quoteVolume24h;
        private Boolean restricted;
        private BigDecimal sizeIncrement;
        private String type;
        private BigDecimal volumeUsd24h;
        private BigDecimal upperBound;
        private BigDecimal volume;
        private String underlying;
        private String underlyingDescription;

    }

}
