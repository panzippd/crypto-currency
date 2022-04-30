package com.crypto.currency.scheduler.service.scheduler;

import com.crypto.currency.scheduler.model.BaseTaskEntity;

import java.util.List;

/**
 * @author Panzi
 * @Description marking scheduler
 * @date 2022/4/30 18:48
 */
public interface IScheduler {

    List<? extends BaseTaskEntity> schedule(String param);
}
