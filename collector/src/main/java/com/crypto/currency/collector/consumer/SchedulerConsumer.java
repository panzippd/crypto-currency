package com.crypto.currency.collector.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.List;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/13 17:21
 */
@Slf4j
public class SchedulerConsumer extends BaseConsumer {
    private volatile KafkaConsumer<String, String> consumer;

    private SchedulerConsumer() {

    }

    @Override
    protected void onClose() {

        if (consumer != null) {
            consumer.wakeup();
        }
    }

    /**
     * the kafka spot consumer
     */
    @Override
    public void run() {

        try {
            consumer = new KafkaConsumer<>(builder.configs);
            consumer.subscribe(List.of(builder.consumerTopic));
            while (!closed.get()) {
                ConsumerRecords<String, String> records = consumer.poll(builder.pollTimeout);
                try {
                    push(consumer, records);
                } catch (Exception ex) {
                    log.error(builder.consumerTopic + "-consumer:", ex);
                }
            }
        } catch (Exception ex) {
            if (ex instanceof WakeupException || closed.get()) {
                log.info("The " + builder.consumerTopic + " consumer shutdown!");
            } else {
                log.error(builder.consumerTopic + "-consumer:", ex);
            }
        } finally {
            consumer.close();
        }
    }

    /**
     * new builder
     *
     * @return
     */
    public static Builder builder() {
        return new Builder(new SchedulerConsumer());
    }
}
