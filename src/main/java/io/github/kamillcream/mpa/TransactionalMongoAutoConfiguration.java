package io.github.kamillcream.mpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.core.MongoTemplate;


@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class TransactionalMongoAutoConfiguration {
    @Bean
    public TransactionalMongoAspect transactionalMongoAspect(
            MongoTemplate mongoTemplate) {
        return new TransactionalMongoAspect(mongoTemplate);
    }
    @Bean
    public MongoEntityLoader mongoEntityLoader(MongoTemplate mongoTemplate) {
        return new MongoEntityLoader(mongoTemplate);
    }
}