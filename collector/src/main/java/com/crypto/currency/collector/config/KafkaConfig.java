package com.crypto.currency.collector.config;

import com.crypto.currency.data.config.KafkaClusterConfig;
import com.crypto.currency.data.config.KafkaCommonConfig;
import com.crypto.currency.data.config.KafkaProducerAndConsumerConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

/**
 * @author Panzi
 * @Description kafka uri
 * @date 2022/5/2 16:33
 */
@Configuration
@ConditionalOnClass(KafkaCommonConfig.class)
@Order(1)
@Slf4j
public class KafkaConfig {

    public static final String TEST_PRODUCER = "testProducer";
    public static final String TEST_Customer = "testConsumer";

    private volatile static KafkaProducerAndConsumerConfig testProducerConfig;

    private volatile static KafkaProducerAndConsumerConfig testConsumerConfig;

    @Bean("kafkaClusterConfig")
    @ConfigurationProperties(prefix = "cmc.crypto.currency.collector.kafka")
    public KafkaClusterConfig kafkaClusterConfig() {
        return new KafkaClusterConfig();
    }

    @Bean("kafkaCommonConfig")
    public KafkaCommonConfig kafkaConfig(@Qualifier("kafkaClusterConfig") KafkaClusterConfig kafkaClusterConfig) {
        return new KafkaCommonConfig(kafkaClusterConfig);
    }

    @Bean("testProducerConfig")
    @ConfigurationProperties("cmc.crypto.currency.collector.kafka.producer.test")
    public KafkaProducerAndConsumerConfig buildTestProducerConfig() {
        testProducerConfig = new KafkaProducerAndConsumerConfig();
        return testProducerConfig;
    }

    @Bean("testConsumerConfig")
    @ConfigurationProperties("cmc.crypto.currency.collector.kafka.consumer.test")
    public KafkaProducerAndConsumerConfig buildTestConsumerConfig() {
        testConsumerConfig = new KafkaProducerAndConsumerConfig();
        return testConsumerConfig;
    }

    @Bean(value = TEST_PRODUCER)
    public KafkaSender<String, String> buildNewSpotCollectorProducer(
        @Qualifier("kafkaCommonConfig") KafkaCommonConfig kafkaCommonConfig) {
        Map<String, Object> props = kafkaCommonConfig.commonProducerConfig();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, testProducerConfig.getClientId());
        SenderOptions<String, String> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }

    public ReceiverOptions<String, String> buildTestSchedulerConsumer(KafkaCommonConfig kafkaCommonConfig) {

        Map<String, Object> props = Maps.newHashMap(kafkaCommonConfig.commonConsumerConfig());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, testConsumerConfig.getClientId());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, testConsumerConfig.getGroupId());
        return ReceiverOptions.create(props);
    }

    public static KafkaProducerAndConsumerConfig getTestConsumerConfig() {
        return testConsumerConfig;
    }

}
