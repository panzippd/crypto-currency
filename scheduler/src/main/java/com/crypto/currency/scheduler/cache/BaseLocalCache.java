package com.crypto.currency.scheduler.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Panzi
 * @Description the base class for local cache
 * @date 2022/4/29 17:56
 */
@Slf4j
public abstract class BaseLocalCache<K, V> implements ApplicationListener<ContextRefreshedEvent> {

    private static final ListeningExecutorService REFRESH_POOLS;
    public static final String INFO_TAG = "refresh local cache:";

    protected volatile LoadingCache<K, V> loadingCache;
    protected final long duration;
    protected final TimeUnit unit;

    static {
        REFRESH_POOLS = MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(2000),
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("RefreshCacheExecutorService-pool-%d").build(),
                new ThreadPoolExecutor.AbortPolicy()));
    }

    BaseLocalCache(long duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
        loadingCache = createCacheLoader(this.duration, this.unit, this::load);
    }

    /**
     * Initializing the loadingCache
     *
     * @param duration
     * @param unit
     * @param loadCache
     */
    protected LoadingCache<K, V> createCacheLoader(long duration, TimeUnit unit, Function<K, V> loadCache) {

        return CacheBuilder.newBuilder().refreshAfterWrite(duration, unit).build(new CacheLoader<K, V>() {
            @Override
            public V load(K key) throws Exception {
                log.info(INFO_TAG + key);
                try {
                    return loadCache.apply(key);
                } catch (Exception ex) {
                    log.error(INFO_TAG + key, ex);
                    throw ex;
                }
            }

            @Override
            public ListenableFuture<V> reload(final K key, V oldValue) {
                log.info("reload-" + INFO_TAG + key);
                return REFRESH_POOLS.submit(() -> loadCache.apply(key));
            }
        });
    }

    /**
     * loading the data
     *
     * @param key
     * @return
     */
    protected abstract V load(K key);

    /**
     * first loading the data after the system startup
     */
    protected void preLoad() {

    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            preLoad();
        }
    }
}
