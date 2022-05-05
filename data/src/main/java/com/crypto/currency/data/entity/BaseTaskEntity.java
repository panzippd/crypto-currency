package com.crypto.currency.data.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Panzi
 * @Description the parent class for task
 * @date 2022/4/30 18:05
 */
@Data
public class BaseTaskEntity {

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime scheduleTime;

    private String tranId;

    public LocalDateTime getScheduleTime() {
        return scheduleTime == null ? LocalDateTime.now() : scheduleTime;
    }

}
