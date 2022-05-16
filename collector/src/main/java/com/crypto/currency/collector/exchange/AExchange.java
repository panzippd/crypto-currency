package com.crypto.currency.collector.exchange;

import com.crypto.currency.collector.entity.ExchangeLogEntity;
import com.crypto.currency.collector.util.SymbolUtils;
import com.crypto.currency.common.http.AsynHttpClient;
import com.crypto.currency.common.http.RxWebSocketClient;
import com.crypto.currency.common.utils.DateTimeUtils;
import com.crypto.currency.common.utils.JacksonUtils;
import com.crypto.currency.data.entity.ExchangeScheduleTaskEntity;
import com.crypto.currency.data.entity.TickerEntity;
import com.crypto.currency.data.enums.DataType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Panzi
 * @Description The abstract exchange
 * @date 2022/5/16 21:15
 */
@Slf4j
public abstract class AExchange implements IExchange {

    public static final String TICKER_LOG = "tickerLog";

    // https://git.coinmarketcap.supply/cmc-ops/infra-docs/-/blob/master/migrate-to-lambda-proxy.md
    @Value("${service.proxy:}")
    public String PROXY_URL;

    @Value("${service.fixed:}")
    public String FIXED_IP;

    @Getter
    @Setter
    private String exchangeName;

    @Getter
    @Setter
    private Integer exchangeId;

    @Getter
    @Setter
    private String tickerUrl;

    @Autowired
    protected SymbolUtils symbolUtils;

    /**
     * custom webclient,usually used for complex definitions;
     */
    @Autowired
    private WebClient client;

    /**
     * default web client, the processing log is written to the database.
     */
    @Autowired
    private AsynHttpClient.DefaultWebClient defaultWebClient;

    @Autowired
    private RxWebSocketClient webSocketClient;

    @Autowired
    private ExchangeLogService logService;

    protected String buildProxyUrl(String url, String region) {
        if (StringUtils.isAnyBlank(url, PROXY_URL)) {
            return null;
        }
        String regionPrefix = StringUtils.equalsIgnoreCase(region, "JP") ? "ap-northeast-1/" : "";

        String newUrl = StringUtils.removeStart(url, "https://");
        newUrl = StringUtils.removeStart(newUrl, "http://");
        return PROXY_URL + regionPrefix + newUrl;
    }

    protected String buildFixedIp(String url) {
        if (StringUtils.isAnyBlank(url, FIXED_IP)) {
            return null;
        }
        String newUrl = StringUtils.removeStart(url, "https://");
        newUrl = StringUtils.removeStart(newUrl, "http://");
        return FIXED_IP + newUrl;
    }

    /**
     * full query
     *
     * @param task
     * @return
     */
    protected boolean isFullQuery(ExchangeScheduleTaskEntity task) {

        if (task == null) {
            return true;
        }
        return StringUtils.isAllBlank(task.getMainSymbol(), task.getBaseSymbol());
    }

    /**
     * get spot ticker datas
     *
     * @param taskEntity
     * @return
     */
    @Override
    public Mono<TickerEntity> getTickerData(final ExchangeScheduleTaskEntity taskEntity) {

        final ExchangeLogEntity tickerLog = buildLog(taskEntity);
        final Long startTime = System.currentTimeMillis();
        ZonedDateTime zonedDateTime = taskEntity.getScheduleTime().atZone(ZoneId.systemDefault());
        return Mono.just(taskEntity).publishOn(Schedulers.boundedElastic()).flatMap(this::getTickers).map(result -> {
                result.setDataType(DataType.SPOT);
                return appendSpotLogResult(tickerLog, result);
            }).subscriberContext(Context.of(TICKER_LOG, tickerLog))
            .doOnError(throwable -> tickerLog.setResponse(throwable.getMessage())).doFinally(f -> {
                log.info("Finished spot data collection: {}", taskEntity);
                tickerLog.setTotalElapsedTime(System.currentTimeMillis() - startTime);
                logService.addLog(tickerLog);
            });
    }

    /**
     * get order books
     *
     * @param task
     * @return
     */
    //    @Override
    //    @Metrics("getOrderBooksData")
    //    public Mono<OrderBookEntity> getOrderBooksData(ExchangeScheduleTaskEntity task) {
    //
    //        final ExchangeLogEntity orderBooksLog = buildLog(task);
    //        final Long startTime = System.currentTimeMillis();
    //        ZonedDateTime zonedDateTime = task.getScheduleTime().atZone(ZoneId.systemDefault());
    //        mr.recordExecutionTime("OrderBookMsg", startTime - zonedDateTime.toInstant().toEpochMilli());
    //        return Mono.just(task).publishOn(Schedulers.boundedElastic()).flatMap(this::getOrderBooks).map(result -> {
    //                result.setDataType(EDataType.ORDER_BOOK);
    //                return appendOrderBooksLogResult(orderBooksLog, result);
    //            }).subscriberContext(Context.of(TICKER_LOG, orderBooksLog))
    //            .doOnError(throwable -> orderBooksLog.setResponse(throwable.getMessage())).doFinally(f -> {
    //                log.info("Finished order book data collection: {}", task);
    //                orderBooksLog.setTotalElapsedTime(System.currentTimeMillis() - startTime);
    //                logService.addLog(orderBooksLog);
    //            });
    //    }

    /**
     * get Swap ticker datas
     *
     * @param taskEntity
     * @return
     */
    @Override
    public Mono<TickerEntity> getSwapData(final ExchangeScheduleTaskEntity taskEntity) {

        final ExchangeLogEntity tickerLog = buildLog(taskEntity);
        final Long startTime = System.currentTimeMillis();
        ZonedDateTime zonedDateTime = taskEntity.getScheduleTime().atZone(ZoneId.systemDefault());
        return Mono.just(taskEntity).publishOn(Schedulers.boundedElastic()).flatMap(this::getSwapTickers)
            .map(result -> {
                result.setDataType(DataType.PERPETUAL);
                return appendSpotLogResult(tickerLog, result);
            }).subscriberContext(Context.of(TICKER_LOG, tickerLog))
            .doOnError(throwable -> tickerLog.setResponse(throwable.getMessage())).doFinally(f -> {
                log.info("Finished swap data collection: {}", taskEntity);
                tickerLog.setTotalElapsedTime(System.currentTimeMillis() - startTime);
                logService.addLog(tickerLog);
            });
    }

    /**
     * get Options ticker datas
     *
     * @param taskEntity
     * @return
     */
    @Override
    public Mono<TickerEntity> getOptionsData(final ExchangeScheduleTaskEntity taskEntity) {

        final ExchangeLogEntity tickerLog = buildLog(taskEntity);
        final Long startTime = System.currentTimeMillis();
        ZonedDateTime zonedDateTime = taskEntity.getScheduleTime().atZone(ZoneId.systemDefault());
        return Mono.just(taskEntity).publishOn(Schedulers.boundedElastic()).flatMap(this::getOptionsTickers)
            .map(result -> {
                result.setDataType(DataType.OPTIONS);
                return appendSpotLogResult(tickerLog, result);
            }).subscriberContext(Context.of(TICKER_LOG, tickerLog))
            .doOnError(throwable -> tickerLog.setResponse(throwable.getMessage())).doFinally(f -> {
                log.info("Finished options data collection: {}", taskEntity);
                tickerLog.setTotalElapsedTime(System.currentTimeMillis() - startTime);
                logService.addLog(tickerLog);
            });
    }

    /**
     * get Perpetual ticker datas
     *
     * @param taskEntity
     * @return
     */
    @Override
    public Mono<TickerEntity> getPerpetualData(final ExchangeScheduleTaskEntity taskEntity) {

        final ExchangeLogEntity tickerLog = buildLog(taskEntity);
        final Long startTime = System.currentTimeMillis();
        ZonedDateTime zonedDateTime = taskEntity.getScheduleTime().atZone(ZoneId.systemDefault());
        return Mono.just(taskEntity).publishOn(Schedulers.boundedElastic()).flatMap(this::getPerpetualTickers)
            .map(result -> {
                result.setDataType(DataType.PERPETUAL);
                return appendSpotLogResult(tickerLog, result);
            }).subscriberContext(Context.of(TICKER_LOG, tickerLog))
            .doOnError(throwable -> tickerLog.setResponse(throwable.getMessage())).doFinally(f -> {
                log.info("Finished perpetual data collection: {}", taskEntity);
                tickerLog.setTotalElapsedTime(System.currentTimeMillis() - startTime);
                logService.addLog(tickerLog);
            });
    }

    /**
     * get future ticker datas
     *
     * @param taskEntity
     * @return
     */
    @Override
    public Mono<TickerEntity> getFuturesData(final ExchangeScheduleTaskEntity taskEntity) {

        final ExchangeLogEntity tickerLog = buildLog(taskEntity);
        final Long startTime = System.currentTimeMillis();
        ZonedDateTime zonedDateTime = taskEntity.getScheduleTime().atZone(ZoneId.systemDefault());
        return Mono.just(taskEntity).publishOn(Schedulers.boundedElastic()).flatMap(this::getFuturesTickers)
            .map(result -> {
                result.setDataType(DataType.FUTURES);
                return appendSpotLogResult(tickerLog, result);
            }).subscriberContext(Context.of(TICKER_LOG, tickerLog))
            .doOnError(throwable -> tickerLog.setResponse(throwable.getMessage())).doFinally(f -> {
                log.info("Finished futures data collection: {}", taskEntity);
                tickerLog.setTotalElapsedTime(System.currentTimeMillis() - startTime);
                logService.addLog(tickerLog);
            });
    }

    @Override
    public Flux<ExchangeScheduleTaskEntity> getTradingPairDatas(final ExchangeScheduleTaskEntity taskEntity) {

        return Mono.just(taskEntity).flatMap(this::getTradingPairs)
            .flatMapMany(result -> Flux.fromIterable(result).onBackpressureBuffer(Math.max(result.size(), 1)));
    }

    /**
     * get the exchange trading pairs.
     *
     * @param taskEntity
     * @return
     */
    protected Mono<List<ExchangeScheduleTaskEntity>> getTradingPairs(final ExchangeScheduleTaskEntity taskEntity) {

        return Mono.empty();
    }

    /**
     * get exchange spot tickers
     *
     * @return
     */
    protected abstract Mono<TickerEntity> getTickers(ExchangeScheduleTaskEntity task);

    /**
     * get exchange derivatives - Swap 永续合约 tickers
     *
     * @return
     */
    protected Mono<TickerEntity> getSwapTickers(ExchangeScheduleTaskEntity task) {

        return Mono.empty();
    }

    /**
     * get exchange derivatives - options 期权 tickers
     *
     * @return
     */
    protected Mono<TickerEntity> getOptionsTickers(ExchangeScheduleTaskEntity task) {

        return Mono.empty();
    }

    /**
     * get exchange derivatives - perpetual 永续合约 tickers
     *
     * @return
     */
    protected Mono<TickerEntity> getPerpetualTickers(ExchangeScheduleTaskEntity task) {

        return Mono.empty();
    }

    /**
     * get exchange derivatives -  future 交割合约 tickers
     *
     * @return
     */
    protected Mono<TickerEntity> getFuturesTickers(ExchangeScheduleTaskEntity task) {

        return Mono.empty();
    }

    /**
     * send get request
     *
     * @param url
     * @return
     */
    protected Mono<String> get(final String url) {
        return Mono.subscriberContext()
            .flatMap(context -> defaultWebClient.get(url).map(r -> appendResponse(url, context, r.getBody())));
    }

    /**
     * send get request,can add header
     *
     * @param url
     * @return
     */
    protected Mono<String> get(final String url, final Consumer<HttpHeaders> header) {

        return Mono.subscriberContext()
            .flatMap(context -> defaultWebClient.get(url, header).map(r -> appendResponse(url, context, r.getBody())));
    }

    /**
     * send post request
     *
     * @param url
     * @return
     */
    protected Mono<String> post(final String url, final Object body) {

        return Mono.subscriberContext().flatMap(context -> defaultWebClient.post(url, body, (h) -> {
        }).map(r -> appendResponse(url, context, r.getBody())));
    }

    /**
     * send post request ,add header
     *
     * @param url
     * @param body
     * @param header
     * @return
     */
    protected Mono<String> post(final String url, final Object body, final Consumer<HttpHeaders> header) {

        return Mono.subscriberContext().flatMap(context -> defaultWebClient.post(url, body, header::accept)
            .map(r -> appendResponse(url, context, r.getBody())));
    }

    /**
     * websocket send.
     *
     * @param url
     * @param body
     * @return
     */
    protected Mono<String> send(String url, String body) {

        return Mono.subscriberContext()
            .flatMap(context -> webSocketClient.send(url, body).map(r -> appendResponse(url, context, r)));
    }

    /**
     * build ticker log
     *
     * @param taskEntity
     * @return
     */
    private ExchangeLogEntity buildLog(ExchangeScheduleTaskEntity taskEntity) {

        return ExchangeLogEntity.builder().code(exchangeId).name(exchangeName)
            .scheduleTime(taskEntity.getScheduleTime()).mainSymbol(taskEntity.getBaseSymbol())
            .baseSymbol(taskEntity.getMainSymbol()).baseId(taskEntity.getBaseId()).mainId(taskEntity.getMainId())
            .createdTime(DateTimeUtils.nowUTC()).responseElapsedTime(System.currentTimeMillis())
            .status(ExchangeLogEntity.EXCHANGE_UNPROCESSED)
            .dataType(taskEntity.getType() == null ? null : taskEntity.getType().getCategory())
            .tranId(taskEntity.getTranId()).build();
    }

    /**
     * appendResponse
     *
     * @param url
     * @param context
     * @param response
     * @return
     */
    private String appendResponse(String url, Context context, String response) {

        Optional<ExchangeLogEntity> log = context.getOrEmpty(TICKER_LOG);
        if (log != null && log.isPresent()) {
            log.get().setResponse(response);
            log.get().setUrl(url);
            log.get().setResponseElapsedTime(System.currentTimeMillis() - log.get().getResponseElapsedTime());
            log.get().setStatus(ExchangeLogEntity.EXCHANGE_HAS_RESPONSE);
            return response;
        }
        return response;
    }

    /**
     * append spot result
     *
     * @param tickerLog
     * @param result
     * @return
     */
    private TickerEntity appendSpotLogResult(ExchangeLogEntity tickerLog, TickerEntity result) {

        tickerLog.setResult(JacksonUtils.serialize(result));
        tickerLog.setStatus(ExchangeLogEntity.EXCHANGE_HAS_RESULT);
        return result;
    }

    /**
     * append orderbooks result
     *
     * @param log
     * @param result
     * @return
     */
    //    private OrderBookEntity appendOrderBooksLogResult(ExchangeLogEntity log, OrderBookEntity result) {
    //
    //        log.setResult(JSON.toJSONString(result));
    //        log.setStatus(ExchangeLogEntity.EXCHANGE_HAS_RESULT);
    //        return result;
    //    }

    /**
     * Checks if a {@link JsonNode} is a valid node to parse into a {@link java.math.BigDecimal}
     *
     * @param node the json node
     * @return if it's valid or not
     */
    protected boolean isValidNumericNode(JsonNode node) {
        return node != null && !"null".equalsIgnoreCase(node.asText());
    }
}
