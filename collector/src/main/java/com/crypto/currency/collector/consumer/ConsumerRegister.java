package com.crypto.currency.collector.consumer;

import com.crypto.currency.common.utils.ThreadPoolFactory;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/10 21:43
 */
@Component
public class ConsumerRegister implements DisposableBean {

    private final ConcurrentMap<BaseConsumer, Boolean> consumerMap = Maps.newConcurrentMap();
    private ExecutorService consumerPool;

    /**
     * register consumer
     *
     * @param consumers
     */
    public void register(BaseConsumer... consumers) {

        if (consumers == null) {
            return;
        }

        for (BaseConsumer consumer : consumers) {
            consumerMap.put(consumer, false);
        }
    }

    /**
     * start consumer
     */
    public void start() {
        int consumerCount = consumerMap.size();
        consumerPool = ThreadPoolFactory.getInstance()
            .createThreadPool("Collector-Kafka-Consumer", consumerCount, consumerCount, 20000, 16);

        for (Map.Entry<BaseConsumer, Boolean> consumer : consumerMap.entrySet()) {
            if (!consumer.getValue()) {
                consumer.setValue(true);
                consumerPool.submit(consumer.getKey());
            }
        }
    }

    @Override
    public void destroy() {

        for (Map.Entry<BaseConsumer, Boolean> consumer : consumerMap.entrySet()) {
            if (consumer.getValue()) {
                consumer.getKey().destroy();
            }
        }
        consumerPool.shutdown();
    }
}
