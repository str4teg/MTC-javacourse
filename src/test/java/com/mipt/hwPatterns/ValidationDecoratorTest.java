package com.mipt.hwPatterns;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationDecoratorTest {

  @Test
  void testFindDataByKey_ValidatesKey() {
    SimpleDataService simpleService = new SimpleDataService();
    ValidationDecorator validationService = new ValidationDecorator(simpleService);

    assertThrows(IllegalArgumentException.class, () -> {
      validationService.findDataByKey(null);
    });

    assertThrows(IllegalArgumentException.class, () -> {
      validationService.findDataByKey("");
    });

    assertThrows(IllegalArgumentException.class, () -> {
      validationService.findDataByKey("   ");
    });
  }

  @Test
  void testSaveData_ValidatesInput() {
    SimpleDataService simpleService = new SimpleDataService();
    ValidationDecorator validationService = new ValidationDecorator(simpleService);

    // Тест на невалидный ключ
    assertThrows(IllegalArgumentException.class, () -> {
      validationService.saveData(null, "data");
    });

    // Тест на невалидные данные
    assertThrows(IllegalArgumentException.class, () -> {
      validationService.saveData("key1", null);
    });

    // Тест на слишком длинный ключ
    String longKey = "a".repeat(101);
    assertThrows(IllegalArgumentException.class, () -> {
      validationService.saveData(longKey, "data");
    });
  }

  @Test
  void testDeleteData_ValidatesKey() {
    SimpleDataService simpleService = new SimpleDataService();
    ValidationDecorator validationService = new ValidationDecorator(simpleService);

    assertThrows(IllegalArgumentException.class, () -> {
      validationService.deleteData(null);
    });

    assertThrows(IllegalArgumentException.class, () -> {
      validationService.deleteData("");
    });
  }

  @Test
  void testValidOperations_Success() {
    SimpleDataService simpleService = new SimpleDataService();
    ValidationDecorator validationService = new ValidationDecorator(simpleService);

    // Валидные операции должны выполняться без исключений
    assertDoesNotThrow(() -> {
      validationService.saveData("key1", "value1");
      validationService.findDataByKey("key1");
      validationService.deleteData("key1");
    });
  }
}