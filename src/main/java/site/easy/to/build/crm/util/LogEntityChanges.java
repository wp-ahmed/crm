package site.easy.to.build.crm.util;

import org.springframework.data.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LogEntityChanges {

    public static Map<String, Pair<String, String>> trackChanges(Object previousState, Object currentState, List<String> properties) {
        Map<String, Pair<String, String>> changes = new HashMap<>();

        for (String property : properties) {
            String propertyName = StringUtils.replaceCharToCamelCase(property, '_');
            propertyName = StringUtils.replaceCharToCamelCase(propertyName, ' ');
            String getterMethodName = "get" + StringUtils.capitalizeFirstLetter(propertyName);

            try {
                Method getterMethod = previousState.getClass().getMethod(getterMethodName);
                Object previousValue =  getterMethod.invoke(previousState);
                Object currentValue =  getterMethod.invoke(currentState);

                if (!Objects.equals(previousValue, currentValue)) {
                    String previousValueAsString = convertToString(previousValue);
                    String currentValueAsString = convertToString(currentValue);

                    changes.put(property, Pair.of(previousValueAsString, currentValueAsString));
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Handle the exception or log the error
            }
        }

        return changes;
    }

    private static String convertToString(Object value) {
        if (value == null) {
            return null;
        }
        if(value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }
}
