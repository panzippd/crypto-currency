package com.crypto.currency.scheduler.config;

import com.crypto.currency.data.config.KafkaClusterConfig;
import com.crypto.currency.data.config.KafkaCommonConfig;
import com.crypto.currency.data.config.KafkaProducerAndConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
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
    public static final String SPOT_PRODUCER = "spotProducer";
    public static final String TEST_Customer = "testConsumer";

    private volatile static KafkaProducerAndConsumerConfig testProducerConfig;
    private volatile static KafkaProducerAndConsumerConfig spotProducerConfig;

    private volatile static KafkaProducerAndConsumerConfig testConsumerConfig;

    @Bean("kafkaClusterConfig")
    @ConfigurationProperties(prefix = "cmc.crypto.currency.scheduler.kafka")
    public KafkaClusterConfig kafkaClusterConfig() {
        return new KafkaClusterConfig();
    }

    @Bean("kafkaCommonConfig")
    public KafkaCommonConfig kafkaConfig(@Qualifier("kafkaClusterConfig") KafkaClusterConfig kafkaClusterConfig) {
        return new KafkaCommonConfig(kafkaClusterConfig);
    }

    @Bean("testProducerConfig")
    @ConfigurationProperties("cmc.crypto.currency.scheduler.kafka.producer.test")
    public KafkaProducerAndConsumerConfig buildTestProducerConfig() {
        testProducerConfig = new KafkaProducerAndConsumerConfig();
        return testProducerConfig;
    }

    @Bean("spotProducerConfig")
    @ConfigurationProperties("cmc.crypto.currency.scheduler.kafka.producer.spot")
    public KafkaProducerAndConsumerConfig buildSpotProducerConfig() {
        spotProducerConfig = new KafkaProducerAndConsumerConfig();
        return spotProducerConfig;
    }

    @Bean("testConsumerConfig")
    @ConfigurationProperties("cmc.crypto.currency.scheduler.kafka.consumer.test")
    public KafkaProducerAndConsumerConfig buildTestConsumerConfig() {
        testConsumerConfig = new KafkaProducerAndConsumerConfig();
        return testConsumerConfig;
    }

    @Bean(value = TEST_PRODUCER)
    public KafkaSender<String, String> buildNewTestCollectorProducer(
        @Qualifier("kafkaCommonConfig") KafkaCommonConfig kafkaCommonConfig) {
        Map<String, Object> props = kafkaCommonConfig.commonProducerConfig();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, testProducerConfig.getClientId());
        SenderOptions<String, String> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }

    @Bean(value = SPOT_PRODUCER)
    public KafkaSender<String, String> buildNewSpotCollectorProducer(
        @Qualifier("kafkaCommonConfig") KafkaCommonConfig kafkaCommonConfig) {
        Map<String, Object> props = kafkaCommonConfig.commonProducerConfig();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, spotProducerConfig.getClientId());
        SenderOptions<String, String> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }

    @Bean(value = TEST_Customer)
    public KafkaReceiver<Object, Object> buildNewSpotCollectorConsumer(
        @Qualifier("kafkaCommonConfig") KafkaCommonConfig kafkaCommonConfig) {
        Map<String, Object> props = kafkaCommonConfig.commonConsumerConfig();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, testConsumerConfig.getClientId());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, testConsumerConfig.getGroupId());
        ReceiverOptions<Object, Object> receiverOptions =
            ReceiverOptions.create(props).subscription(Collections.singleton(testConsumerConfig.getTopic()))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned : {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked : {}", partitions));
        return KafkaReceiver.create(receiverOptions);
    }

}
