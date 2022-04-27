package com.crypto.currency.data.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Panzi
 * @Description The exchange base data
 * @date 2022/4/27 13:44
 */
@Data
@Document(collection = "exchange")
public class ExchangeEntity {

    @Id
    private ObjectId id;
    private Integer exchangeId;
    private String name;
    private String slug;
    private String homepageUrl;
    private BigDecimal spotsVolumeUsd;
    private BigDecimal derivativesVolumeUsd;
    private Integer markets;
    private Integer coins;
    private Boolean isActive;
    private Date createdTime;
    private Date updateTime;
}
