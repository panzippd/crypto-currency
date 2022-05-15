package com.crypto.currency.collector.consumer.event.handler;

import com.crypto.currency.collector.config.KafkaConfig;
import com.crypto.currency.collector.consumer.event.SchedulerTaskEvent;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * @author Panzi
 * @Description test
 * @date 2022/5/14 21:12
 */
@Slf4j
public class TestEventHandler implements WorkHandler<SchedulerTaskEvent> {

    @Autowired
    private static volatile ApplicationContext context;

    @Override
    public void onEvent(SchedulerTaskEvent event) throws Exception {
        if (event == null || StringUtils.isBlank(event.getData())) {
            return;
        }
        KafkaSender<String, String> newCollectorSpotProducer =
            (KafkaSender<String, String>)context.getBean(KafkaConfig.TEST_PRODUCER);
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

        //        final IExchange exchange = FunctionalFactory.getExchange(taskEntity.getExchangeId().toString());
        //        Publisher<SenderRecord<String, String, TickerEntity>> tickerPublisher;
        //        if (taskEntity.isTakeTradingPair()) {
        //            tickerPublisher = exchange.getTradingPairDatas(taskEntity).flatMap(t -> {
        //                t.setScheduleTime(taskEntity.getScheduleTime());
        //                t.setTranId(taskEntity.getTranId());
        //                return exchange.getTickerData(t);
        //            }).map(result -> {
        //                result.setPushTime(DateTimeUtils.nowUTC());
        //                return SenderRecord.create(
        //                    new ProducerRecord<>(KafkaConfig.getSpotProducerConfig().getTopic(), ExtUtils.uuid(),
        //                        JSON.toJSONString(result)), result);
        //            });
        //        } else {
        //            tickerPublisher = Mono.just(taskEntity).flatMap(t -> exchange.getTickerData(taskEntity)).map(r -> {
        //                r.setPushTime(ExtUtils.nowUTC());
        //                List<CMCTicker> cmcTickers = r.getCmcTickers();
        //                if (!CollectionUtils.isEmpty(cmcTickers) && cmcTickers.size() > THRESHOLD) {
        //                    List<List<CMCTicker>> partition = ListUtils.partition(cmcTickers, THRESHOLD);
        //                    return partition.stream().map(sub -> {
        //                        TickerEntity entity = new TickerEntity();
        //                        BeanUtils.copyProperties(r, entity);
        //                        entity.setCmcTickers(sub);
        //                        return SenderRecord.create(
        //                            new ProducerRecord<>(KafkaConfig.getSpotProducerConfig().getTopic(), ExtUtils.uuid(),
        //                                JSON.toJSONString(entity)), entity);
        //                    }).collect(Collectors.toList());
        //                } else {
        //                    return List.of(SenderRecord.create(
        //                        new ProducerRecord<>(KafkaConfig.getSpotProducerConfig().getTopic(), ExtUtils.uuid(),
        //                            JSON.toJSONString(r)), r));
        //                }
        //            }).flatMapMany(r -> Flux.fromIterable(r));
        //        }
        //        return tickerPublisher;
        return null;
    }
}
