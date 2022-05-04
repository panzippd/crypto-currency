package com.crypto.currency.collector.consumer.event;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/4 21:16
 */
@Slf4j
public class EventHandlerException implements ExceptionHandler {

    @Override
    public void handleEventException(Throwable throwable, long sequence, Object event) {
        log.error("disruptor error sequence:{}, event:{} ,error:{}", sequence, event, throwable);
    }

    @Override
    public void handleOnStartException(Throwable throwable) {
        log.error("start disruptor error:{}", throwable.getMessage(), throwable);
    }

    @Override
    public void handleOnShutdownException(Throwable throwable) {
        log.error("shutdown disruptor error:{}", throwable.getMessage(), throwable);
    }
}
