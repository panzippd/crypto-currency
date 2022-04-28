package com.crypto.currency.scheduler.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;

/**
 * @author Panzi
 * @Description multiole config properties
 * @date 2022/4/28 17:45
 */
public class MultipleMongoProperties {

    private MongoProperties cryptoCurrency = new MongoProperties();

    public MongoProperties getCryptoCurrency() {
        return cryptoCurrency;
    }

    public void setCryptoCurrency(MongoProperties cryptoCurrency) {
        this.cryptoCurrency = cryptoCurrency;
    }
}
