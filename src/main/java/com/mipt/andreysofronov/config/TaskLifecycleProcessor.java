package com.mipt.andreysofronov.config;

import com.mipt.andreysofronov.repository.TaskRepository;
import com.mipt.andreysofronov.repository.TaskJpaRepository;
import com.mipt.andreysofronov.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class TaskLifecycleProcessor implements BeanPostProcessor {

  private static final Logger log = LoggerFactory.getLogger(TaskLifecycleProcessor.class);

  @Override
  public Object postProcessBeforeInitialization(
      @NonNull Object bean, @NonNull String beanName) throws BeansException {
    if (matches(bean)) {
      log.info(
          "[{}] Создание/подготовка бина завершены, до init-callbacks: beanName={}, class={}",
          kind(bean),
          beanName,
          bean.getClass().getName());
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(
      @NonNull Object bean, @NonNull String beanName) throws BeansException {
    if (matches(bean)) {
      log.info(
          "[{}] Инициализация бина завершена (после init-callbacks): beanName={}, class={}",
          kind(bean),
          beanName,
          bean.getClass().getName());
    }
    return bean;
  }

  private static boolean matches(Object bean) {
    return bean instanceof TaskService || bean instanceof TaskRepository || bean instanceof TaskJpaRepository;
  }

  private static String kind(Object bean) {
    if (bean instanceof TaskService) {
      return "TaskService";
    }
    return "TaskRepository";
  }
}
