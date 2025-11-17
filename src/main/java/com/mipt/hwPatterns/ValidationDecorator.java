package com.mipt.hwPatterns;

import java.util.Optional;

public class ValidationDecorator implements DataService {
  private final DataService dataService;

  public ValidationDecorator(DataService dataService) {
    this.dataService = dataService;
  }

  @Override
  public Optional<String> findDataByKey(String key) {
    validateKey(key);
    return dataService.findDataByKey(key);
  }

  @Override
  public void saveData(String key, String data) {
    validateKey(key);
    validateData(data);
    dataService.saveData(key, data);
  }

  @Override
  public boolean deleteData(String key) {
    validateKey(key);
    return dataService.deleteData(key);
  }

  private void validateKey(String key) {
    if (key == null || key.trim().isEmpty()) {
      throw new IllegalArgumentException("Ключ не может быть пустым или null");
    }
    if (key.length() > 100) {
      throw new IllegalArgumentException("Ключ слишком длинный (максимум 100 символов)");
    }
  }

  private void validateData(String data) {
    if (data == null) {
      throw new IllegalArgumentException("Данные не могут быть null");
    }
    if (data.length() > 10000) {
      throw new IllegalArgumentException("Данные слишком большие (максимум 10000 символов)");
    }
  }
}