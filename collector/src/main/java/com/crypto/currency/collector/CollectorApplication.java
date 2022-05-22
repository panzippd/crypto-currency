package com.crypto.currency.collector;

import com.crypto.currency.collector.support.annotation.FunctionalScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.crypto.currency.collector", "com.crypto.currency.common"})
@FunctionalScan(basePackages = {"com.cmc.worker.collector.exchange", "com.cmc.worker.collector.crypto"})
public class CollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }

}
