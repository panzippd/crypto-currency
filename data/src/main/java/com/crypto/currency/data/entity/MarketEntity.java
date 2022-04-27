package com.crypto.currency.data.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Panzi
 * @Description The marketPair data.Exchange has some marketPairs, a marketPair consists of 2 cryptoCurrency. eg:BTC/USDT
 * @date 2022/4/27 13:44
 */
@Data
@Document(collection = "market")
public class MarketEntity {

    @Id
    private ObjectId id;
    private Integer marketId;
    private Integer exchangeId;
    private Integer baseCryptoCurrencyId;
    private String baseSymbol;
    private String baseSymbolOverride;
    private Integer mainCryptoCurrencyId;
    private String mainSymbol;
    private String mainSymbolOverride;
    /**
     * Spot,Perpetual,Future,Option
     */
    private String category;
    private BigDecimal priceUsd;
    private BigDecimal priceNative;
    private BigDecimal volumeBase;
    private BigDecimal volumeMain;
    private BigDecimal volumeUsd;
    private Boolean isActive;
    private Date createdTime;
    private Date updateTime;
}
