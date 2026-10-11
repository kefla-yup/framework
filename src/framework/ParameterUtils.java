package framework;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class ParameterUtils {

    public static Object bindParameter(Parameter parameter, HttpServletRequest request) throws Exception {
        Class<?> targetType = parameter.getType();
        String paramName = parameter.getName(); 

        if (!isPrimitiveOrWrapperOrString(targetType) && !targetType.isArray() && !List.class.isAssignableFrom(targetType)) {
            return bindInternal(targetType, "", parameter.getParameterizedType(), request);
        }

        return bindInternal(targetType, paramName, parameter.getParameterizedType(), request);
    }

    private static Object bindInternal(Class<?> targetType, String currentPrefix, java.lang.reflect.Type genericType, HttpServletRequest request) throws Exception {
        
        if (targetType.isArray()) {
            String[] values = request.getParameterValues(currentPrefix);
            if (values == null) return null;
            
            Class<?> componentType = targetType.getComponentType();
            Object arrayInstance = java.lang.reflect.Array.newInstance(componentType, values.length);
            for (int i = 0; i < values.length; i++) {
                java.lang.reflect.Array.set(arrayInstance, i, convertValue(values[i], componentType));
            }
            return arrayInstance;
        }

        if (List.class.isAssignableFrom(targetType)) {
            String[] values = request.getParameterValues(currentPrefix);
            if (values == null) return null;
            
            List<Object> list = new ArrayList<>();
            Class<?> itemType = String.class; 
            try {
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) genericType;
                    itemType = (Class<?>) paramType.getActualTypeArguments()[0];
                }
            } catch (Exception ignored) {}

            for (String val : values) {
                list.add(convertValue(val, itemType));
            }
            return list;
        }

        if (isPrimitiveOrWrapperOrString(targetType)) {
            String value = request.getParameter(currentPrefix);
            return convertValue(value, targetType);
        }

        Object complexObj = targetType.getDeclaredConstructor().newInstance();
        Field[] fields = targetType.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            
            String subPrefix = (currentPrefix != null && !currentPrefix.isEmpty()) ? currentPrefix + "." + fieldName : fieldName;

            if (isPrimitiveOrWrapperOrString(field.getType())) {
                String httpVal = request.getParameter(subPrefix);
                if (httpVal != null) {
                    Object converted = convertValue(httpVal, field.getType());
                    field.set(complexObj, converted);
                }
            } else {
                Object nestedObj = bindInternal(field.getType(), subPrefix, field.getGenericType(), request);
                field.set(complexObj, nestedObj);
            }
        }

        return complexObj;
    }

    public static Object convertValue(String value, Class<?> targetType) {
        if (value == null || value.trim().isEmpty()) {
            if (targetType == int.class) return 0;
            if (targetType == double.class) return 0.0;
            if (targetType == boolean.class) return false;
            if (targetType == float.class) return 0.0f;
            if (targetType == long.class) return 0L;
            return null;
        }

        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }

        return value;
    }

    private static boolean isPrimitiveOrWrapperOrString(Class<?> type) {
        return type.isPrimitive() || 
               type == String.class || 
               type == Integer.class || 
               type == Double.class || 
               type == Boolean.class || 
               type == Float.class || 
               type == Long.class;
    }
}