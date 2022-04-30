package com.crypto.currency.scheduler.controller;

import com.crypto.currency.data.entity.ExchangeEntity;
import com.crypto.currency.data.enums.DataType;
import com.crypto.currency.data.repository.mongo.cryptocurrency.ExchangeRepository;
import com.crypto.currency.scheduler.service.DispatcherScheduler;
import com.crypto.currency.scheduler.service.scheduler.ExchangeSpotScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author Panzi
 * @Description manage exchange scheduler
 * @date 2022/4/28 18:18
 */
@Slf4j
@RestController
@RequestMapping("/exchange")
public class ExchangeController {

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private DispatcherScheduler dispatcher;

    @GetMapping("/query")
    public Mono<List<ExchangeEntity>> getExchange() {
        return exchangeRepository.findByIsActive(true).collectList();
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void doExchangeSpotScheduler() {
        log.info("the doExchangeSpotScheduler start,params={}", DataType.SPOT.getCategory());
        dispatcher.doDispatch(ExchangeSpotScheduler.class, "spotProducer", "worker_scheduler_spot_prod",
            DataType.SPOT.getCategory());
    }

}
