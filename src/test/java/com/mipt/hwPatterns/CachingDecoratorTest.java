package com.mipt.hwPatterns;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;

class CachingDecoratorTest {

  @Test
  void testFindDataByKey_CachesResult() {
    SimpleDataService simpleService = new SimpleDataService();
    CachingDecorator cachingService = new CachingDecorator(simpleService);

    simpleService.saveData("key1", "value1");

    // Первый вызов - должен обратиться к базовому сервису
    Optional<String> result1 = cachingService.findDataByKey("key1");
    assertEquals("value1", result1.get());

    // Второй вызов - должен вернуть данные из кэша
    Optional<String> result2 = cachingService.findDataByKey("key1");
    assertEquals("value1", result2.get());
  }

  @Test
  void testSaveData_UpdatesCache() {
    SimpleDataService simpleService = new SimpleDataService();
    CachingDecorator cachingService = new CachingDecorator(simpleService);

    cachingService.saveData("key1", "value1");

    // Данные должны быть в кэше после сохранения
    Optional<String> result = cachingService.findDataByKey("key1");
    assertEquals("value1", result.get());
  }

  @Test
  void testDeleteData_InvalidatesCache() {
    SimpleDataService simpleService = new SimpleDataService();
    CachingDecorator cachingService = new CachingDecorator(simpleService);

    simpleService.saveData("key1", "value1");
    cachingService.findDataByKey("key1"); // Добавляем в кэш

    cachingService.deleteData("key1");

    // После удаления данные не должны быть в кэше
    Optional<String> result = cachingService.findDataByKey("key1");
    assertFalse(result.isPresent());
  }
}
