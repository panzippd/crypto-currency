package com.crypto.currency.collector.support;

import com.crypto.currency.collector.crypto.base.ISupply;
import com.crypto.currency.collector.exchange.IExchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author Panzi
 * @Description 注入和获取调用交易所或项目方数据接口的实现类
 * @date 2022/5/15 21:45
 */
public class FunctionalFactory {

    @Autowired
    private static volatile ApplicationContext context;

    public static IExchange getExchange(String id) {

        if (StringUtils.isBlank(id)) {
            return null;
        }
        return (IExchange)context.getBean(BeanScanHandler.EXCHANGE + id);
    }

    public static ISupply getSupply(String id) {

        if (StringUtils.isBlank(id)) {
            return null;
        }
        return (ISupply)context.getBean(BeanScanHandler.CRYPTO + id);
    }

}
