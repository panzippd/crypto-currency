package com.crypto.currency.collector.consumer.event;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/4 21:09
 */
public class DisruptorBuilder {
    public static <T> Builder<T> builder() {

        return new Builder<T>();
    }

    public static class Builder<T> {
        private Integer ringBufferSize = 16;
        private WaitStrategy waitStrategy = null;
        private WorkHandler<T>[] eventHandler;
        private ProducerType producerType = null;
        private ThreadFactory executorService = null;

        public Builder<T> ringBufferSize(Integer ringBufferSize) {

            this.ringBufferSize = ringBufferSize;
            return this;
        }

        public Builder<T> waitStrategy(WaitStrategy waitStrategy) {

            this.waitStrategy = waitStrategy;
            return this;
        }

        public Builder<T> eventHandler(WorkHandler<T>... eventHandler) {

            Objects.requireNonNull(eventHandler);
            this.eventHandler = eventHandler;
            return this;
        }

        public Builder<T> producerType(ProducerType producerType) {

            this.producerType = producerType;
            return this;
        }

        public Builder<T> executorService(ThreadFactory executorService) {

            this.executorService = executorService;
            return this;
        }

        public Disruptor<T> build() {

            Disruptor<T> disruptor =
                new Disruptor<T>(new SchedulerTaskFactory(), ObjectUtils.defaultIfNull(ringBufferSize, 16),
                    ObjectUtils.defaultIfNull(executorService, Executors.defaultThreadFactory()),
                    ObjectUtils.defaultIfNull(producerType, ProducerType.SINGLE),
                    ObjectUtils.defaultIfNull(waitStrategy, new BlockingWaitStrategy()));
            disruptor.setDefaultExceptionHandler(new EventHandlerException());
            disruptor.handleEventsWithWorkerPool(eventHandler);
            return disruptor;
        }
    }
}
