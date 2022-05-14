package com.crypto.currency.collector.consumer;

import com.crypto.currency.collector.config.KafkaConfig;
import com.crypto.currency.collector.consumer.event.handler.TestEventHandler;
import com.crypto.currency.data.config.KafkaCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/14 21:06
 */
@Component
@Slf4j
public class ConsumerConfigure implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${com.worker.collector.kafka.disable:false}")
    private String disableConsumer;

    @Autowired
    private ConsumerRegister consumerRegister;

    @Autowired
    private KafkaConfig kafkaConfig;
    @Autowired
    @Qualifier("kafkaCommonConfig")
    private KafkaCommonConfig kafkaCommonConfig;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            init();
        }
    }

    public void init() {

        if ("true".equalsIgnoreCase(disableConsumer)) {
            log.info("worker.collector.kafka.disable:{}", disableConsumer);
            return;
        }

        consumerRegister.register(
            /**
             *  test
             */
            SchedulerConsumer.builder()
                .configure(kafkaConfig.buildTestSchedulerConsumer(kafkaCommonConfig).consumerProperties())
                .consumerTopic(KafkaConfig.getTestConsumerConfig().getTopic()).pollTimeout(Duration.ofMillis(500))
                .ringBufferSize(16).workHandler(new TestEventHandler()).build());
        consumerRegister.start();
    }
}
