package com.mipt.hw8;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Size {
  int min() default 2;
  int max() default 50;
  String message();
}