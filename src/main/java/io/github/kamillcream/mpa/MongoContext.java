package io.github.kamillcream.mpa;

public class MongoContext {
    private static final ThreadLocal<MongoUnitOfWork> CONTEXT = new ThreadLocal<>();

    public static void set(MongoUnitOfWork uow) {
        CONTEXT.set(uow);
    }

    public static MongoUnitOfWork get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
