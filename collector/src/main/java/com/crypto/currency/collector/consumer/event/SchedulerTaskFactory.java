package com.crypto.currency.collector.consumer.event;

import com.lmax.disruptor.EventFactory;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/4 21:11
 */
public class SchedulerTaskFactory implements EventFactory {

    @Override
    public Object newInstance() {
        return new SchedulerTaskEvent();
    }
}
