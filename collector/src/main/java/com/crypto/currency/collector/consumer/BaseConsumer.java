package com.crypto.currency.collector.consumer;

import com.crypto.currency.collector.consumer.event.DisruptorBuilder;
import com.crypto.currency.collector.consumer.event.SchedulerTaskEvent;
import com.crypto.currency.common.utils.CollectionUtils;
import com.google.common.collect.Maps;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Panzi
 * @Description the base consumer
 * @date 2022/5/4 18:34
 */
@Slf4j
public abstract class BaseConsumer implements Runnable {
    private volatile Disruptor<?> disruptor;
    protected volatile Builder builder;
    /**
     * Represents whether the current exit consumption
     */
    protected final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * close event;
     *
     * @throws Exception
     */
    public void destroy() {

        try {
            closed.set(true);
            onClose();
        } catch (Exception ex) {
            log.error("baseConsumer-destroy", ex);
        }
        if (this.disruptor != null) {
            this.disruptor.shutdown();
        }
    }

    /**
     * close kafka producer or consumer
     */
    protected abstract void onClose();

    /**
     * get disruptor
     *
     * @return
     */
    protected Disruptor<?> getDisruptor() {
        return this.disruptor;
    }

    /**
     * start consumer
     *
     * @param builder
     */
    protected void start(Builder builder) {

        disruptor = DisruptorBuilder.builder().eventHandler(builder.workHandlers).ringBufferSize(builder.ringBufferSize)
            .build();
        disruptor.start();
    }

    /**
     * push to processor
     *
     * @param records
     */
    protected void push(final KafkaConsumer consumer, ConsumerRecords<String, String> records) {

        if (records == null || records.isEmpty()) {
            return;
        }
        RingBuffer<SchedulerTaskEvent> ringBuffer = (RingBuffer<SchedulerTaskEvent>)this.getDisruptor().getRingBuffer();
        for (TopicPartition partition : records.partitions()) {

            List<ConsumerRecord<String, String>> messages = records.records(partition);
            if (CollectionUtils.isEmpty(messages)) {
                continue;
            }
            for (ConsumerRecord<String, String> message : messages) {
                ringBuffer.publishEvent((event, sequence, data) -> event.setData(data), message.value());
                consumer.commitAsync(Map.of(partition, new OffsetAndMetadata(message.offset() + 1)), (p, ex) -> {
                    if (ex != null) {
                        log.error("partition:{},error:{}", p, ex);
                    }
                });
            }
        }
    }

    public static class Builder {
        private final BaseConsumer consumer;
        protected final Map<String, Object> configs = Maps.newHashMap();
        protected int ringBufferSize;
        protected WorkHandler[] workHandlers;
        protected String consumerTopic;
        protected Duration pollTimeout;

        public Builder(BaseConsumer consumer) {

            this.consumer = consumer;
        }

        /**
         * custom kafka consumer config.
         *
         * @param configs
         * @return
         */
        public Builder configure(Map<String, Object> configs) {

            if (CollectionUtils.isEmpty(configs)) {
                return this;
            }
            this.configs.putAll(configs);
            return this;
        }

        public Builder ringBufferSize(int size) {

            this.ringBufferSize = size;
            return this;
        }

        public Builder workHandler(WorkHandler... workHandlers) {

            this.workHandlers = workHandlers;
            return this;
        }

        public Builder consumerTopic(String topic) {

            this.consumerTopic = topic;
            return this;
        }

        public Builder pollTimeout(Duration duration) {

            this.pollTimeout = duration;
            return this;
        }

        public BaseConsumer build() {
            consumer.builder = this;
            consumer.start(this);
            return consumer;
        }
    }
}
