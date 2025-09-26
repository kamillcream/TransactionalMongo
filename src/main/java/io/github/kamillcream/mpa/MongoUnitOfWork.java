package io.github.kamillcream.mpa;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

import java.util.Map;

public class MongoUnitOfWork implements AutoCloseable {
    private final Map<Object, Object> snapshots = new HashMap<>();


    public <T> T register(T entity) {
        snapshots.put(entity, deepCopy(entity));
        return entity;
    }

    public void commit(MongoTemplate template) {
        for (var entry : snapshots.entrySet()) {
            Object current = entry.getKey();
            Object original = entry.getValue();

            Map<String, Object> changes = DiffUtil.calculateDiff(original, current);

            String lastModifiedFieldName = getLastModifiedFieldName(current);
            if (lastModifiedFieldName != null) {
                changes.remove(lastModifiedFieldName);
            }

            boolean hasLastModified = hasLastModifiedField(current);
            if (changes.isEmpty() && !hasLastModified) continue;

            Update update = new Update();

            changes.forEach((field, value) -> {
                if (value != null) {
                    update.set(field, value);
                } else {
                    update.unset(field);
                }
            });

            if (hasLastModified) {
                LocalDateTime utcNow = LocalDateTime.now(ZoneId.of("UTC"));

                // 메모리 객체 업데이트
                setLastModifiedDate(current);

                // MongoDB 쿼리에 추가
                update.set(lastModifiedFieldName, utcNow);
            }

            Object idValue = extractIdValue(current);
            Criteria criteria = createIdCriteria(idValue);

            template.updateFirst(
                    Query.query(criteria),
                    update,
                    current.getClass()
            );

        }
    }

    @Override
    public void close() {
        snapshots.clear();
    }

    private Object deepCopy(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.convertValue(obj, obj.getClass());
    }

    private Object extractIdValue(Object entity) {
        Class<?> c = entity.getClass();
        while (c != null && c != Object.class) {
            for (var f : c.getDeclaredFields()) {
                if (f.isAnnotationPresent(org.springframework.data.annotation.Id.class)) {
                    f.setAccessible(true);
                    try { return f.get(entity); }
                    catch (IllegalAccessException e) { throw new RuntimeException(e); }
                }
            }
            c = c.getSuperclass();
        }
        throw new IllegalStateException("The @Id field could not be found: " + entity.getClass());
    }

    private Criteria createIdCriteria(Object idValue) {
        if (idValue instanceof String && ObjectId.isValid((String) idValue)) {
            return new Criteria().orOperator(
                    Criteria.where("_id").is(idValue),
                    Criteria.where("_id").is(new ObjectId((String) idValue))
            );
        }
        return Criteria.where("_id").is(idValue);
    }

    private void setLastModifiedDate(Object entity) {
        Field field = findFieldWithAnnotation(entity.getClass(), LastModifiedDate.class);
        if (field != null && field.getType() == LocalDateTime.class) {
            field.setAccessible(true);
            try {
                field.set(entity, LocalDateTime.now(ZoneId.of("UTC")));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to set @LastModifiedDate", e);
            }
        }
    }

    private boolean hasLastModifiedField(Object entity) {
        return findFieldWithAnnotation(entity.getClass(), LastModifiedDate.class) != null;
    }

    private String getLastModifiedFieldName(Object entity) {
        Field field = findFieldWithAnnotation(entity.getClass(), LastModifiedDate.class);
        return field != null ? field.getName() : null;
    }

    private Field findFieldWithAnnotation(Class<?> clazz, Class<? extends java.lang.annotation.Annotation> annotationClass) {
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotationClass) && field.getType() == LocalDateTime.class) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

}
