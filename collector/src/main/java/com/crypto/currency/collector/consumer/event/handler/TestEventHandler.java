package com.crypto.currency.collector.consumer.event.handler;

import com.crypto.currency.collector.config.KafkaConfig;
import com.crypto.currency.collector.consumer.event.SchedulerTaskEvent;
import com.crypto.currency.collector.exchange.IExchange;
import com.crypto.currency.collector.support.FunctionalFactory;
import com.crypto.currency.common.utils.CollectionUtils;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.common.utils.SpringBeanUtils;
import com.crypto.currency.common.utils.StringUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.reactivestreams.Publisher;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Panzi
 * @Description test
 * @date 2022/5/14 21:12
 */
@Slf4j
public class TestEventHandler implements WorkHandler<SchedulerTaskEvent> {

    // threshold of ticker size for kafka msg
    private final static int THRESHOLD = 100;

    @Override
    public void onEvent(SchedulerTaskEvent event) throws Exception {
        if (event == null || StringUtils.isBlank(event.getData())) {
            return;
        }
        KafkaSender<String, String> newCollectorSpotProducer =
            (KafkaSender<String, String>)SpringBeanUtils.getBean(KafkaConfig.TEST_PRODUCER);
        final ExchangeScheduleTaskEntity taskEntity =
            JacksonUtils.deserialize(event.getData(), ExchangeScheduleTaskEntity.class);

        sendMessage(newCollectorSpotProducer, taskEntity);
    }

    private void sendMessage(KafkaSender<String, String> producer, ExchangeScheduleTaskEntity taskEntity) {
        log.info("producer={},taskEntity={}", producer, taskEntity.toString());
    }

    /**
     * @param taskEntity
     * @return
     */
    private Publisher<SenderRecord<String, String, TickerEntity>> buildSenderRecordPublisher(
        ExchangeScheduleTaskEntity taskEntity) {

        final IExchange exchange = FunctionalFactory.getExchange(taskEntity.getExchangeId().toString());
        Publisher<SenderRecord<String, String, TickerEntity>> tickerPublisher;
        tickerPublisher = Mono.just(taskEntity).flatMap(t -> exchange.getTickerData(taskEntity)).map(r -> {
            r.setPushTime(DateTimeUtils.nowUTC());
            List<TickerEntity.CMCTicker> cmcTickers = r.getCmcTickers();
            if (!CollectionUtils.isEmpty(cmcTickers) && cmcTickers.size() > THRESHOLD) {
                List<List<TickerEntity.CMCTicker>> partition =
                    CollectionUtils.groupListByQuantity(cmcTickers, THRESHOLD);
                return partition.stream().map(sub -> {
                    TickerEntity entity = new TickerEntity();
                    BeanUtils.copyProperties(r, entity);
                    entity.setCmcTickers(sub);
                    return SenderRecord.create(
                        new ProducerRecord<>(KafkaConfig.getTestConsumerConfig().getTopic(), StringUtils.uuid(),
                            JacksonUtils.serialize(entity)), entity);
                }).collect(Collectors.toList());
            } else {
                return List.of(SenderRecord.create(
                    new ProducerRecord<>(KafkaConfig.getTestConsumerConfig().getTopic(), StringUtils.uuid(),
                        JacksonUtils.serialize(r)), r));
            }
        }).flatMapMany(r -> Flux.fromIterable(r));
        return tickerPublisher;
    }
}
