package com.crypto.currency.scheduler.model;

import com.crypto.currency.data.enums.DataType;
import lombok.Builder;
import lombok.Data;

/**
 * @author Panzi
 * @Description the exchange task data
 * @date 2022/4/30 18:08
 */
@Data
@Builder
public class ExchangeScheduleTaskEntity extends BaseTaskEntity {

    private Integer exchangeId;

    private String exchangeName;

    private String baseSymbol;

    private String mainSymbol;

    private Integer mainId;

    private Integer baseId;

    private DataType type;

    /**
     * true : get exchange trading pair.
     * false : No collection
     */
    private boolean takeTradingPair;
}
