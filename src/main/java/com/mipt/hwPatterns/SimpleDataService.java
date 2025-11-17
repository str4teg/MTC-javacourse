package com.mipt.hwPatterns;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

interface DataService {
  Optional<String> findDataByKey(String key);

  void saveData(String key, String data);

  boolean deleteData(String key);
}

class SimpleDataService implements DataService {
  private Map<String, String> storage = new HashMap<>();

  @Override
  public Optional<String> findDataByKey(String key) {
    return Optional.ofNullable(storage.get(key));
  }

  @Override
  public void saveData(String key, String data) {
    storage.put(key, data);
  }

  @Override
  public boolean deleteData(String key) {
    return storage.remove(key) != null;
  }
}