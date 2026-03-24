package com.mipt.andreysofronov.aspect;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  @Around(
      "execution(* com.mipt.andreysofronov.service..*(..)) "
          + "&& !within(com.mipt.andreysofronov.aspect.LoggingAspect)")
  public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
    String method = joinPoint.getSignature().toShortString();
    log.info("Старт: {} | аргументы={}", method, Arrays.toString(joinPoint.getArgs()));

    long startNanos = System.nanoTime();
    try {
      Object result = joinPoint.proceed();
      long millis = (System.nanoTime() - startNanos) / 1_000_000L;
      log.info(
          "Конец: {} | {} | за {} мс",
          method,
          describeReturnValue(joinPoint, result),
          millis);
      return result;
    } catch (Throwable ex) {
      long millis = (System.nanoTime() - startNanos) / 1_000_000L;
      log.error(
          "Конец с исключением: {} | за {} мс | {}",
          method,
          millis,
          ex.toString());
      throw ex;
    }
  }

  private static String describeReturnValue(ProceedingJoinPoint joinPoint, Object result) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Class<?> returnType = signature.getReturnType();
    if (returnType == void.class) {
      return "возвращаемого значения нет (void)";
    }
    if (result == null) {
      return "результат: null";
    }
    return "результат: " + result;
  }
}
