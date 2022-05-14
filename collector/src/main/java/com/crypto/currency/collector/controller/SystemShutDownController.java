package com.crypto.currency.collector.controller;

import com.crypto.currency.collector.consumer.ConsumerRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Panzi
 * @Description some todo when shut down
 * @date 2022/5/9 22:59
 */
@Service
public class SystemShutDownController {

    @Autowired
    private ConsumerRegister consumerRegister;

    public void shutDownAll() {

        shutDownKafkaConsumer();
    }

    private void shutDownKafkaConsumer() {
        consumerRegister.destroy();
    }
}
