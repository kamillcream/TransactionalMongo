package io.github.kamillcream.mpa;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class MongoEntityLoader {
    private final MongoTemplate mongoTemplate;

    public MongoEntityLoader(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public <T> T findById(String id, Class<T> clazz) {
        T entity = mongoTemplate.findById(id, clazz);
        MongoUnitOfWork uow = MongoContext.get();
        if (uow != null && entity != null) {
            uow.register(entity);
        }
        return entity;
    }

    public <T> T findOneByField(String fieldName, Object value, Class<T> clazz) {
        Query query = new Query(Criteria.where(fieldName).is(value));
        T entity = mongoTemplate.findOne(query, clazz);
        registerToUnitOfWork(entity);
        return entity;
    }

    private <T> void registerToUnitOfWork(T entity) {
        MongoUnitOfWork uow = MongoContext.get();
        if (uow != null && entity != null) {
            uow.register(entity);
        }
    }
}
