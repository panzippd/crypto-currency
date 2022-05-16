package com.crypto.currency.collector.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author Panzi
 * @Description log entity
 * @date 2022/5/16 21:25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExchangeLogEntity {
    public static final Integer EXCHANGE_HAS_ERROR = 3;
    public static final Integer EXCHANGE_HAS_RESPONSE = 2;
    public static final Integer EXCHANGE_HAS_RESULT = 1;
    public static final Integer EXCHANGE_UNPROCESSED = 0;
    public final static String Table_name = "exchange_log";

    private String id;

    private Integer code;

    private String name;

    private String url;

    private String mainSymbol;

    private String baseSymbol;

    private Integer mainId;

    private Integer baseId;

    private LocalDateTime scheduleTime;
    /**
     * EXCHANGE_TICKER_HAS_RESPONSE = 2;
     * EXCHANGE_TICKER_HAS_RESULT = 1;
     * EXCHANGE_TICKER_UNPROCESSED = 0;
     */
    private Integer status;

    private LocalDateTime updatedTime;

    private LocalDateTime pushTime;

    private String dataType;

    private String request;

    private String response;

    private String result;

    private String tranId;

    private LocalDateTime createdTime;

    private Long responseElapsedTime;

    private Long totalElapsedTime;
}
