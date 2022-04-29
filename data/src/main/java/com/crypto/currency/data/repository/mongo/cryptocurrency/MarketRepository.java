package com.crypto.currency.data.repository.mongo.cryptocurrency;

import com.crypto.currency.data.entity.MarketEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * @author Panzi
 * @Description market repository
 * @date 2022/4/28 18:03
 */
public interface MarketRepository
    extends ReactiveMongoRepository<MarketEntity, ObjectId>, ReactiveCrudRepository<MarketEntity, ObjectId> {

    Flux<MarketEntity> findByIsActive(Boolean isActive);
}
