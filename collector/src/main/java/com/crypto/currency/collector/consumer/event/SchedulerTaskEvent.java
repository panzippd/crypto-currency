package com.crypto.currency.collector.consumer.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/4 21:13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchedulerTaskEvent {
    /**
     * data
     */
    private String data;
}
