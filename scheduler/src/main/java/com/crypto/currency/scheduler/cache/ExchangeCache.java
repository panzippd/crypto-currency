package com.crypto.currency.scheduler.cache;

import com.crypto.currency.common.utils.StringUtils;
import com.crypto.currency.scheduler.model.ExchangeInfoDTO;
import com.crypto.currency.scheduler.service.ExchangeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Panzi
 * @Description loading exchange cache
 * @date 2022/4/29 18:22
 */
@Slf4j
@Component
public class ExchangeCache extends BaseLocalCache<String, List<ExchangeInfoDTO>> {

    @Autowired
    private ExchangeService exchangeService;

    /**
     * Cache expires in 30 minutes
     */
    ExchangeCache() {
        super(RandomUtils.nextInt(10, 20), TimeUnit.MINUTES);
    }

    @Override
    protected List<ExchangeInfoDTO> load(String key) {
        return exchangeService.getExchanges();
    }

    @SneakyThrows
    public List<ExchangeInfoDTO> get() {
        return loadingCache.get(StringUtils.EMPTY);
    }

    @Override
    protected void preLoad() {
        log.info("Preload the trade pair data.");
        get();
    }
}
