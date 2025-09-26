package io.github.kamillcream.mpa;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TransactionalMongoAspect {
    private final MongoTemplate mongoTemplate;

    public TransactionalMongoAspect(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Around("@annotation(TransactionalMongo)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        try (MongoUnitOfWork uow = new MongoUnitOfWork()) {
            MongoContext.set(uow);

            Object result = pjp.proceed();

            uow.commit(mongoTemplate);
            return result;
        } finally {
            MongoContext.clear();
        }
    }
}
