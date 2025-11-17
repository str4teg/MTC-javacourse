package com.mipt.hwPatterns;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class LoggingDecoratorTest {

  @Test
  void testFindDataByKey_LogsAction() {
    SimpleDataService simpleService = new SimpleDataService();
    LoggingDecorator loggingService = new LoggingDecorator(simpleService);

    simpleService.saveData("key1", "value1");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    loggingService.findDataByKey("key1");

    System.setOut(originalOut);
    String output = outputStream.toString();

    assertTrue(output.contains("Поиск данных по ключу: key1"));
    assertTrue(output.contains("Результат поиска: найден"));
  }

  @Test
  void testSaveData_LogsAction() {
    SimpleDataService simpleService = new SimpleDataService();
    LoggingDecorator loggingService = new LoggingDecorator(simpleService);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    loggingService.saveData("key1", "value1");

    System.setOut(originalOut);
    String output = outputStream.toString();

    assertTrue(output.contains("Сохранение данных. Ключ: key1, Данные: value1"));
    assertTrue(output.contains("Данные успешно сохранены"));
  }

  @Test
  void testDeleteData_LogsAction() {
    SimpleDataService simpleService = new SimpleDataService();
    LoggingDecorator loggingService = new LoggingDecorator(simpleService);

    simpleService.saveData("key1", "value1");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    loggingService.deleteData("key1");

    System.setOut(originalOut);
    String output = outputStream.toString();

    assertTrue(output.contains("Удаление данных по ключу: key1"));
    assertTrue(output.contains("Результат удаления: успешно"));
  }
}