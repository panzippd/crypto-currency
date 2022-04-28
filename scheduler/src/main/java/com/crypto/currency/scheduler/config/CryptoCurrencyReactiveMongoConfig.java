package com.crypto.currency.scheduler.config;

import com.mongodb.*;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import java.util.concurrent.TimeUnit;

/**
 * @author Panzi
 * @Description db: cryptoCurrency
 * @date 2022/4/28 17:53
 */
@Configuration
@Slf4j
@EnableReactiveMongoRepositories(basePackages = {
    "com.crypto.currency.data.repository.mongo.cryptocurrency"}, reactiveMongoTemplateRef = "cryptoCurrencyReactiveMongoTemplate")
public class CryptoCurrencyReactiveMongoConfig extends BaseReactiveMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getCryptoCurrency().getDatabase();
    }

    @Override
    public MongoClient reactiveMongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoProperties.getCryptoCurrency().getUri());
        MongoCredential credential = connectionString.getCredential();

        MongoClientSettings settings = MongoClientSettings.builder().applyToServerSettings(
                builder -> builder.heartbeatFrequency(10000, TimeUnit.MILLISECONDS)
                    .minHeartbeatFrequency(500, TimeUnit.MILLISECONDS).build()).applyConnectionString(connectionString)
            .credential(credential).applyToConnectionPoolSettings(
                builder -> builder.minSize(5).maxSize(30).maxWaitTime(60000, TimeUnit.MILLISECONDS).build())
            .applyToSocketSettings(builder -> builder.connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS).build()).readPreference(ReadPreference.secondaryPreferred())
            .writeConcern(WriteConcern.MAJORITY).build();

        return MongoClients.create(settings);
    }

    @Bean(name = "cryptoCurrencyReactiveMongoTemplate")
    public ReactiveMongoTemplate reactiveMongoTemplate() throws Exception {
        ReactiveMongoDatabaseFactory factory = reactiveMongoDbFactory();
        ReactiveMongoTemplate reactiveMongoOperations =
            new ReactiveMongoTemplate(factory, mappingMongoConverter(factory));
        reactiveMongoOperations.setReadPreference(ReadPreference.secondaryPreferred());
        return reactiveMongoOperations;
    }
}
