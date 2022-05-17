package com.crypto.currency.data.entity;

import com.crypto.currency.data.enums.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Panzi
 * @Description kafka ticker message
 * @date 2022/5/5 23:16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TickerEntity {

    /**
     * exchangeId	int	是交易ID
     */
    private Integer exchangeId;

    private Integer platformId;

    /**
     * exchangeName	string	是交易所
     */
    private String exchangeName;

    /**
     * pushTime	Date	是推送时间
     */
    private LocalDateTime pushTime;

    /**
     * 最新获取时间
     */
    private LocalDateTime updatedTime;

    private List<CMCTicker> cmcTickers;

    private List<DerivativesTicker> derivativesTicker;

    private DataType dataType;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CMCTicker {

        /**
         * BTC_usdt
         * main = btc, base = usdt
         */
        private String baseSymbol;
        private String mainSymbol;

        /**
         * was called price_main in w1, btc price
         */
        private BigDecimal quote;

        /**
         * was called volume_base in w1, usdt volume
         */
        private BigDecimal mainVolume;

        private String baseContract;
        private String mainContract;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DerivativesTicker {

        private String productType;
        private String tickerId;

        /**
         * 交割时间类型,周,月,季度等,future有值
         * perpetual为默认值PERPETUAL
         */
        private String deliveryTime;
        private String baseSymbol;
        private String mainSymbol;

        /**
         * was called price_main in w1
         */
        private BigDecimal quote;

        /**
         * was called volume_base in w1
         */
        private BigDecimal baseVolume;
        private BigDecimal mainVolume;
        /**
         * 如果usdVolume没有值,mainVolume有值,usdVolume = mainVolume * mainVolume对应的币的USD价格
         */
        private BigDecimal usdVolume;

        private BigDecimal bid;
        private BigDecimal ask;

        private BigDecimal high;
        private BigDecimal low;

        private BigDecimal openInterest;
        private BigDecimal open_InterestUsd;
        /**
         * 如果openInterestSymbol有值,open_InterestUsd=open_InterestUsd * openInterestSymbol对应的币的USD价格
         */
        private String openInterestSymbol;

        private BigDecimal indexPrice;

        private LocalDateTime creationTimestamp;
        /**
         * expiryTimestamp要有值,如果是永续合约,默认值为 4133376000000 (2100年)
         */
        private LocalDateTime expiryTimestamp;

        private BigDecimal fundingRate;
        private BigDecimal makerFee;
        private BigDecimal takerFee;
    }
}
