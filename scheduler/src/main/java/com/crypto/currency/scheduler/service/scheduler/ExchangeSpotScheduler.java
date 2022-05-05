package com.crypto.currency.scheduler.service.scheduler;

import com.crypto.currency.common.utils.CollectionUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.enums.DataType;
import com.crypto.currency.scheduler.cache.ExchangeCache;
import com.crypto.currency.scheduler.model.ExchangeInfoDTO;
import com.crypto.currency.scheduler.model.MarketPairDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Panzi
 * @Description exchange spot data scheduler
 * @date 2022/4/30 18:52
 */
@Slf4j
@Component
public class ExchangeSpotScheduler implements IScheduler {

    @Autowired
    private ExchangeCache exchangeCache;

    /**
     * @param param = dataType
     * @return
     */
    @Override
    public List<ExchangeScheduleTaskEntity> schedule(String param) {
        log.info("ExchangeSpotScheduler.schedule, start");
        DataType type = DataType.getEdataTypeByCategory(param);
        if (null == type) {
            return null;
        }
        return exchangeCache.get().stream().filter(e -> this.checkType(e, param)).map(
            exchange -> ExchangeScheduleTaskEntity.builder().exchangeId(exchange.getExchangeId())
                .exchangeName(exchange.getExchangeName()).type(type).build()).collect(Collectors.toList());
    }

    protected boolean checkType(ExchangeInfoDTO exchangeInfoDTO, String param) {
        if (CollectionUtils.isEmpty(exchangeInfoDTO.getPairs())) {
            return false;
        }
        for (MarketPairDTO dto : exchangeInfoDTO.getPairs()) {
            if (param.equals(dto.getCategory())) {
                return true;
            }
        }
        return false;
    }
}
