package com.crypto.currency.scheduler.controller;

import com.crypto.currency.data.entity.ExchangeEntity;
import com.crypto.currency.data.repository.mongo.cryptocurrency.ExchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author Panzi
 * @Description query exchange data
 * @date 2022/4/28 18:18
 */
@RestController
@RequestMapping("/exchange")
public class ExchangeController {

    @Autowired
    private ExchangeRepository exchangeRepository;

    @GetMapping("/query")
    public Mono<List<ExchangeEntity>> getExchange() {
        return exchangeRepository.findByIsActive(true).collectList();
    }
}
