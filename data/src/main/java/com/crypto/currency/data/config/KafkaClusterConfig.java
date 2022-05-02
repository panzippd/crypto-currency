package com.crypto.currency.data.config;

import lombok.Data;

/**
 * @author Panzi
 * @Description The config info for a specific kafka cluster
 * @date 2022/5/2 17:33
 */
@Data
public class KafkaClusterConfig {
    private String bootstrapServers;
    /**
     * common kafka producer
     */
    private int requestTimeout;
    private String acks;
    /**
     * common kafka consumer
     */
    private int sessionTimeout;
    private int heartbeatInterval;
    private int maxRecords;
    private int maxPollInterval;
    private boolean enableAutoCommit;
}
