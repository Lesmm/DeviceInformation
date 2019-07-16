package common.modules.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IReflectUtilWrapper {

    public static void setWholeFieldsToNull(List<Object> objs, List<String> exceptFields) {
        for (int i = 0; i < objs.size(); i++) {
            Object obj = objs.get(i);
            setWholeFieldsToNull(obj, exceptFields);
        }
    }

    public static void setWholeFieldsToNull(final Object obj, final List<String> exceptFields) {
        Boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();
        IReflectUtil.iterateFields(clazz, new IReflectUtil.IterateFieldHandler() {
            @Override
            public boolean action(Class<?> clazz, Field field, String fieldName) {
                if (exceptFields.contains(fieldName)) {
                    return false;
                }
                try {
                    field.set(obj, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    public static List<Map<?, ?>> getFieldsValues(List<Object> objs, List<String> needFields) {
        List result = new ArrayList();
        for (int i = 0; i < objs.size(); i++) {
            Object obj = objs.get(i);
            Map<?, ?> map = getFieldsValues(obj, needFields);
            result.add(map);
        }
        return result;
    }

    public static Map<?, ?> getFieldsValues(Object obj, List<String> needFields) {
        Map result = new HashMap();
        for (int i = 0; i < needFields.size(); i++) {
            String fieldName = needFields.get(i);
            Object value = IReflectUtil.getFieldValue(obj, fieldName);
            if (value != null) {
                result.put(fieldName, value);
            }
        }
        return result;
    }

}
