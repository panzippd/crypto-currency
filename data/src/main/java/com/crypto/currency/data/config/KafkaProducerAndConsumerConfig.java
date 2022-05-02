package com.crypto.currency.data.config;

import lombok.Data;

/**
 * @author Panzi
 * @Description The config info for producer and consumer
 * @date 2022/5/2 17:43
 */
@Data
public class KafkaProducerAndConsumerConfig {
    /**
     * common field
     */
    private String clientId;

    private String topic;

    /**
     * only consumer field
     */
    private String groupId;
}
