package com.crypto.currency.collector.consumer.event.handler;

import com.crypto.currency.collector.config.KafkaConfig;
import com.crypto.currency.collector.consumer.event.SchedulerTaskEvent;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import reactor.kafka.sender.KafkaSender;

/**
 * @author Panzi
 * @Description test
 * @date 2022/5/14 21:12
 */
@Slf4j
public class TestEventHandler implements WorkHandler<SchedulerTaskEvent> {

    @Autowired
    private ApplicationContext context;

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
        //todo
    }
}
