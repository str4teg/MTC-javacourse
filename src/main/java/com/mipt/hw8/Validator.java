package com.mipt.hw8;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class Validator {
  public static ValidationResult validate(Object object) {
    ValidationResult result = new ValidationResult();

    Class<?> clazz = object.getClass();
    Field[] fields = clazz.getDeclaredFields();

    for (Field field : fields) {
      field.setAccessible(true);

      try {
        Object value = field.get(object);

        if (field.isAnnotationPresent(NotNull.class)) {
          NotNull ann = field.getAnnotation(NotNull.class);
          if (value == null) {
            result.addError(ann.message());
          }
        }

        if (field.isAnnotationPresent(Size.class) && value != null) {
          Size ann = field.getAnnotation(Size.class);
          String v = value.toString();
          if (v.length() < ann.min() || v.length() > ann.max()) {
            result.addError(ann.message());
          }
        }

        if (field.isAnnotationPresent(Range.class) && value != null) {
          Range ann = field.getAnnotation(Range.class);
          if (value instanceof Number num) {
            double val = num.doubleValue();
            if (val < ann.min() || val > ann.max()) {
              result.addError(ann.message());
            }
          }
        }

        if (field.isAnnotationPresent(Email.class) && value != null) {
          final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
          Email ann = field.getAnnotation(Email.class);
          if (!Pattern.matches(EMAIL_PATTERN, (String) value)) {
            result.addError(ann.message());
          }
        }


      } catch (IllegalAccessException e) {
        result.addError("Failed to access field " + field.getName());
      }
    }

    return result;
  }
}
