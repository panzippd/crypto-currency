package com.crypto.currency.data.entity;

import com.crypto.currency.data.enums.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Panzi
 * @Description the exchange task data
 * @date 2022/4/30 18:08
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
