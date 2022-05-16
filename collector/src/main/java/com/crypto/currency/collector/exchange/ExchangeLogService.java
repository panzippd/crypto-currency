package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.entity.ExchangeLogEntity;
import com.crypto.currency.common.utils.CollectionUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Panzi
 * @Description log seice\
 * @date 2022/5/16 21:22
 */
@Slf4j
@Service
public class ExchangeLogService {

    private final ArrayBlockingQueue<ExchangeLogEntity> logPool = new ArrayBlockingQueue(2000);

    @Value("${cmc.collector.log.batch.size:100}")
    private int size = 100;

    @Value("${cmc.collector.log.enable:false}")
    private boolean enable;

    //    @Autowired
    //    private ExchangeLogRepository exchangeLogRepository;

    @PostConstruct
    public void init() {
        autoWriteLog();
    }

    /**
     * the log pool is automatically written to db every 1 second.
     */
    private void autoWriteLog() {

        if (!enable) {
            log.info("Database logs are not started.");
            return;
        }
        CompletableFuture.runAsync(() -> {
            log.info("Database logs are started.");
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000L);
                    writeLog(0);
                } catch (Exception ex) {
                    log.error("write to dynamodb failure.{}", ex);
                }
            }
        });
    }

    @PreDestroy
    public void destroy() {
        writeLog(0);
    }

    /**
     * add db log
     *
     * @param l
     * @return
     */
    public void addLog(final ExchangeLogEntity l) {

        if (!enable) {
            log.debug("Collector-Exchange-Log:{}", l);
            return;
        }
        try {
            logPool.add(l);
            writeLog(size);
        } catch (Exception ex) {
            log.error("write to dynamodb failure.{}", ex);
        }
    }

    /**
     * write log
     */
    private void writeLog(int size) {

        if (logPool.size() > size) {
            List<ExchangeLogEntity> logs = Lists.newArrayListWithCapacity(size);
            logPool.drainTo(logs, size);
            if (CollectionUtils.isEmpty(logs)) {
                return;
            }
            //            exchangeLogRepository.batchWriteItem(logs).exceptionally(throwable -> {
            //                log.error("write to dynamodb failure.{}", throwable);
            //                return false;
            //            });
        }
    }

}
