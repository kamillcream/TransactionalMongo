package io.github.kamillcream.mpa;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DiffUtil {

    public static Map<String, Object> calculateDiff(Object original, Object current) {
        Map<String, Object> changes = new HashMap<>();

        if (original == null || current == null) return changes;

        Class<?> clazz = original.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            try {
                Object origVal = field.get(original);
                Object currVal = field.get(current);

                if (currVal != null && !equals(origVal, currVal)) {
                    changes.put(field.getName(), currVal);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return changes;
    }

    private static boolean equals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
