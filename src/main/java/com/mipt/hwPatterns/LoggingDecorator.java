package com.mipt.hwPatterns;

import java.util.Optional;

public class LoggingDecorator implements DataService {
  private final DataService dataService;

  public LoggingDecorator(DataService dataService) {
    this.dataService = dataService;
  }

  @Override
  public Optional<String> findDataByKey(String key) {
    System.out.println("Поиск данных по ключу: " + key);
    Optional<String> result = dataService.findDataByKey(key);
    System.out.println("Результат поиска: " + (result.isPresent() ? "найден" : "не найден"));
    return result;
  }

  @Override
  public void saveData(String key, String data) {
    System.out.println("Сохранение данных. Ключ: " + key + ", Данные: " + data);
    dataService.saveData(key, data);
    System.out.println("Данные успешно сохранены");
  }

  @Override
  public boolean deleteData(String key) {
    System.out.println("Удаление данных по ключу: " + key);
    boolean result = dataService.deleteData(key);
    System.out.println("Результат удаления: " + (result ? "успешно" : "ключ не найден"));
    return result;
  }
}