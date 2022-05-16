package com.crypto.currency.collector.support;

import com.crypto.currency.collector.crypto.base.ISupply;
import com.crypto.currency.collector.exchange.IExchange;
import com.crypto.currency.common.utils.SpringBeanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Panzi
 * @Description 注入和获取调用交易所或项目方数据接口的实现类
 * @date 2022/5/15 21:45
 */
public class FunctionalFactory {

    public static IExchange getExchange(String id) {

        if (StringUtils.isBlank(id)) {
            return null;
        }
        return (IExchange)SpringBeanUtils.getBean(BeanScanHandler.EXCHANGE + id);
    }

    public static ISupply getSupply(String id) {

        if (StringUtils.isBlank(id)) {
            return null;
        }
        return (ISupply)SpringBeanUtils.getBean(BeanScanHandler.CRYPTO + id);
    }

}
