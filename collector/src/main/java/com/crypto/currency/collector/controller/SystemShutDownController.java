package com.crypto.currency.collector.controller;

import org.springframework.stereotype.Service;

/**
 * @author Panzi
 * @Description some todo when shut down
 * @date 2022/5/9 22:59
 */
@Service
public class SystemShutDownController {

    //    @Autowired
    //    private ConsumerRegister consumerRegister;

    public void shutDownAll() {

        shutDownKafkaConsumer();
    }

    private void shutDownKafkaConsumer() {
        //        consumerRegister.destroy();
    }
}
