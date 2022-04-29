package com.crypto.currency.scheduler.model;

import lombok.Data;

import java.util.List;

/**
 * @author Panzi
 * @Description exchange info and market pairs
 * @date 2022/4/29 18:20
 */
@Data
public class ExchangeInfoDTO {

    private Integer exchangeId;

    private String exchangeName;

    private List<MarketPairDTO> pairs;
}
