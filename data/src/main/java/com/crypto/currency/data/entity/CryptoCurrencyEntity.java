package com.crypto.currency.data.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Panzi
 * @Description The cryptoCurrency base data
 * @date 2022/4/27 13:44
 */
@Data
@Document(collection = "crypto_currency")
public class CryptoCurrencyEntity {

    @Id
    private ObjectId id;
    private Integer cryptoCurrencyId;
    private String name;
    private String slug;
    private String symbol;
    private String homepageUrl;
    /**
     * Coin,Token,Fiat
     */
    private String category;
    private BigDecimal priceUsd;
    private BigDecimal maxSupply;
    private BigDecimal totalSupply;
    private BigDecimal circulatingSupply;
    private BigDecimal spotsVolumeUsd;
    private BigDecimal derivativesVolumeUsd;
    private Boolean isActive;
    private Date createdTime;
    private Date updateTime;
}
