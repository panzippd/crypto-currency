package com.crypto.currency.data.repository.mongo.cryptocurrency;

import com.crypto.currency.data.entity.ExchangeEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * @author Panzi
 * @Description exchange repository
 * @date 2022/4/28 18:03
 */
@Repository
public interface ExchangeRepository
    extends ReactiveMongoRepository<ExchangeEntity, ObjectId>, ReactiveCrudRepository<ExchangeEntity, ObjectId> {

    Flux<ExchangeEntity> findByIsActive(Boolean isActive);
}
