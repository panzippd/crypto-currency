package com.crypto.currency.scheduler.config;

import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Collections;

/**
 * @author Panzi
 * @Description The ReactiveMongo config common function
 * @date 2022/4/28 17:41
 */
@Configuration
public abstract class BaseReactiveMongoConfiguration {
    private static final DbRefResolver NO_OP_REF_RESOLVER = NoOpDbRefResolver.INSTANCE;

    @Autowired
    protected MultipleMongoProperties mongoProperties;

    /**
     * Return the Reactive Streams {@link MongoClient} instance to connect to. Annotate with {@link Bean} in case you want
     * to expose a {@link MongoClient} instance to the {@link org.springframework.context.ApplicationContext}.
     *
     * @return never {@literal null}.
     */
    public abstract MongoClient reactiveMongoClient();

    /**
     * Return the name of the database to connect to.
     *
     * @return must not be {@literal null}.
     */
    protected abstract String getDatabaseName();

    /**
     * Creates a {@link ReactiveMongoDatabaseFactory} to be used by the {@link ReactiveMongoOperations}. Will use the
     * {@link MongoClient} instance configured in {@link #reactiveMongoClient()}.
     *
     * @return never {@literal null}.
     * @see #reactiveMongoClient()
     */
    public ReactiveMongoDatabaseFactory reactiveMongoDbFactory() {
        return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
    }

    protected MappingMongoConverter mappingMongoConverter(ReactiveMongoDatabaseFactory factory) {

        MongoCustomConversions conversions = new MongoCustomConversions(Collections.emptyList());

        MongoMappingContext context = new MongoMappingContext();
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        context.afterPropertiesSet();

        MappingMongoConverter converter = new MappingMongoConverter(NO_OP_REF_RESOLVER, context);
        converter.setCustomConversions(conversions);
        converter.setCodecRegistryProvider(factory);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        converter.afterPropertiesSet();

        return converter;
    }

}
