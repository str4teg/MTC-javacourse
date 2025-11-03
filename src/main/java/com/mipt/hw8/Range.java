package com.mipt.hw8;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
  int min() default 2;
  int max() default 50;
  String message();
}