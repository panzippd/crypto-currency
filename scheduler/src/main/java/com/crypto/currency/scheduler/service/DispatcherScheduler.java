package com.crypto.currency.scheduler.service;

import com.crypto.currency.common.exception.BusinessException;
import com.crypto.currency.common.utils.CollectionUtils;
import com.crypto.currency.common.utils.ExtUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.scheduler.model.BaseTaskEntity;
import com.crypto.currency.scheduler.service.scheduler.IScheduler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.time.Duration;
import java.util.List;

/**
 * @author Panzi
 * @Description do scheduler and send data
 * @date 2022/4/30 20:13
 */
@Slf4j
@Component
public class DispatcherScheduler {

    @Autowired
    private ApplicationContext context;

    /**
     * dispatch task to collector
     *
     * @param bean  The Schuduler of Servie class Type
     * @param topic The kafka's topic name
     * @param param The xxjob's param
     */
    public void doDispatch(Class<? extends IScheduler> bean, String producer, final String topic, String param) {

        if (bean == null || StringUtils.isBlank(topic)) {
            BusinessException.throwIfMessage("bean or topic is null");
        }
        IScheduler scheduler = context.getBean(bean);
        List<BaseTaskEntity> tasks = (List<BaseTaskEntity>)scheduler.schedule(param);
        log.info("Try to schedule, topic: {}, task: {}", topic, tasks.toString());
        send(producer, buildMessages(tasks, topic)).subscribe();

    }

    /**
     * @param tasks
     * @param topic
     * @return
     */
    private Flux<SenderRecord<String, String, BaseTaskEntity>> buildMessages(List<BaseTaskEntity> tasks,
        final String topic) {

        if (CollectionUtils.isEmpty(tasks)) {
            BusinessException.throwIfMessage("task is null");
        }
        return Flux.fromStream(tasks.stream()).onBackpressureBuffer(tasks.size()).map(m -> {
            m.setTranId(ExtUtils.uuid());
            m.setScheduleTime(ExtUtils.nowUTC());
            return SenderRecord.create(new ProducerRecord<>(topic, m.getTranId(), JacksonUtils.toJson(m)), m);
        });
    }

    /**
     * @param producerName
     * @param messages
     * @return
     */
    private Flux<SenderResult<BaseTaskEntity>> send(String producerName,
        Flux<SenderRecord<String, String, BaseTaskEntity>> messages) {

        KafkaSender<String, String> producer = (KafkaSender<String, String>)context.getBean(producerName);
        return producer.send(messages).delayElements(Duration.ofMillis(7))
            .switchIfEmpty(Mono.error(new BusinessException("Unkown.")))
            .doOnNext(r -> log.info("task:{},succeed!", r.correlationMetadata())).onErrorContinue((throwable, s) -> {
                // TODO send rest request for failure.
                log.error("Task:{},Error:{}", s, throwable);
            });
    }
}
