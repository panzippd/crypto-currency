package com.crypto.currency.scheduler.service;

import com.crypto.currency.common.exception.BusinessException;
import com.crypto.currency.scheduler.model.BaseTaskEntity;
import com.crypto.currency.scheduler.service.scheduler.IScheduler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

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
        //        SpringBeanUtils.getBean(bean);
        List<BaseTaskEntity> tasks = (List<BaseTaskEntity>)scheduler.schedule(param);   //todo task父类没有赋值
        //        log.info("Try to schedule, topic: {}, task: {}", topic, new Gson().toJson(tasks));
        //        send(producer, buildMessages(tasks, topic)).subscribe();
        log.info("Try to schedule, topic: {}, task: {}", topic, tasks.toString()); //todo JsonStream.serialize(tasks)报错

    }
}
