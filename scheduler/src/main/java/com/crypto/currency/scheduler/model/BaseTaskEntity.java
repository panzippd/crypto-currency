package com.crypto.currency.scheduler.model;

import java.time.LocalDateTime;

/**
 * @author Panzi
 * @Description the parent class for task
 * @date 2022/4/30 18:05
 */
public class BaseTaskEntity {

    private LocalDateTime scheduleTime;

    private String tranId;

    public LocalDateTime getScheduleTime() {
        return scheduleTime == null ? LocalDateTime.now() : scheduleTime;
    }

}
