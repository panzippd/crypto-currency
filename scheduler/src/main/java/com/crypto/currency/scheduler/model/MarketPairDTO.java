package com.crypto.currency.scheduler.model;

import lombok.Data;

/**
 * @author Panzi
 * @Description market pair
 * @date 2022/4/29 18:18
 */
@Data
public class MarketPairDTO {

    private Integer mainId;

    private Integer baseId;

    private String mainSymbol;

    private String baseSymbol;

    private String category;
}
