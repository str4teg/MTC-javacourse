package com.mipt.hwPatterns;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class MetricableDecoratorTest {

  @Test
  void testFindDataByKey_SendsMetric() {
    SimpleDataService simpleService = new SimpleDataService();
    MetricableDecorator metricService = new MetricableDecorator(simpleService);

    simpleService.saveData("key1", "value1");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    metricService.findDataByKey("key1");

    System.setOut(originalOut);
    String output = outputStream.toString();

    assertTrue(output.contains("Метод выполнялся: PT"));
  }

  @Test
  void testSaveData_SendsMetric() {
    SimpleDataService simpleService = new SimpleDataService();
    MetricableDecorator metricService = new MetricableDecorator(simpleService);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    metricService.saveData("key1", "value1");

    System.setOut(originalOut);
    String output = outputStream.toString();

    assertTrue(output.contains("Метод выполнялся: PT"));
  }

  @Test
  void testDeleteData_SendsMetric() {
    SimpleDataService simpleService = new SimpleDataService();
    MetricableDecorator metricService = new MetricableDecorator(simpleService);

    simpleService.saveData("key1", "value1");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    metricService.deleteData("key1");

    System.setOut(originalOut);
    String output = outputStream.toString();

    assertTrue(output.contains("Метод выполнялся: PT"));
  }
}