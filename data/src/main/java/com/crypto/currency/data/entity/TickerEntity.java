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
     * exchangeId
     */
    private Integer exchangeId;

    private Integer platformId;

    /**
     * exchangeName
     */
    private String exchangeName;

    /**
     * pushTime	Date
     */
    private LocalDateTime pushTime;

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
         * only future
         * perpetual Default Value PERPETUAL
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
         * usdVolume = mainVolume * mainSymbol's  price of USD
         */
        private BigDecimal usdVolume;

        private BigDecimal bid;
        private BigDecimal ask;

        private BigDecimal high;
        private BigDecimal low;

        private BigDecimal openInterest;
        private BigDecimal open_InterestUsd;
        /**
         * if openInterestSymbol have value,open_InterestUsd=open_InterestUsd * openInterestSymbol's  price of USD
         */
        private String openInterestSymbol;

        private BigDecimal indexPrice;

        private LocalDateTime creationTimestamp;
        /**
         * expiryTimestamp not null ,the perpetual, default value 4133376000000 (2100 year)
         */
        private LocalDateTime expiryTimestamp;

        private BigDecimal fundingRate;
        private BigDecimal makerFee;
        private BigDecimal takerFee;
    }
}
