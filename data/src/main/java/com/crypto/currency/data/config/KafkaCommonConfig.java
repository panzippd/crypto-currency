package com.crypto.currency.data.config;

import com.google.common.collect.Maps;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;

/**
 * @author Panzi
 * @Description The common config for producer and consumer
 * @date 2022/5/2 17:49
 */
public class KafkaCommonConfig {
    private KafkaClusterConfig clusterConfig;

    public KafkaCommonConfig(KafkaClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    /**
     * common consumer config
     *
     * @return
     */
    public Map<String, Object> commonConsumerConfig() {

        Map<String, Object> props = Maps.newHashMap();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, clusterConfig.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, clusterConfig.getSessionTimeout());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, clusterConfig.getHeartbeatInterval());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, clusterConfig.getMaxRecords());
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, clusterConfig.getRequestTimeout());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, clusterConfig.isEnableAutoCommit());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, clusterConfig.getMaxPollInterval());
        return props;
    }

    /**
     * common producer config
     *
     * @return
     */
    public Map<String, Object> commonProducerConfig() {

        Map<String, Object> props = Maps.newHashMap();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, clusterConfig.getBootstrapServers());
        props.put(ProducerConfig.ACKS_CONFIG, clusterConfig.getAcks());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, clusterConfig.getRequestTimeout());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }
}
