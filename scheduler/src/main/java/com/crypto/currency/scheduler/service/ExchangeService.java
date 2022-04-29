package com.crypto.currency.scheduler.service;

import com.crypto.currency.data.repository.mongo.cryptocurrency.ExchangeRepository;
import com.crypto.currency.data.repository.mongo.cryptocurrency.MarketRepository;
import com.crypto.currency.scheduler.model.ExchangeInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Panzi
 * @Description processing exchange data
 * @date 2022/4/29 19:00
 */
public class ExchangeService {

    @Autowired
    private ExchangeRepository exchangeRepository;
    @Autowired
    private MarketRepository marketRepository;

    /**
     * get all exchange data
     *
     * @return
     */
    public List<ExchangeInfoDTO> getExchanges() {

        return null;
    }
}
