package com.mipt.hwPatterns;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CachingDecorator implements DataService {
  private final DataService dataService;
  private final Map<String, String> cache = new HashMap<>();

  public CachingDecorator(DataService dataService) {
    this.dataService = dataService;
  }

  @Override
  public Optional<String> findDataByKey(String key) {
    if (cache.containsKey(key)) {
      return Optional.of(cache.get(key));
    }

    Optional<String> result = dataService.findDataByKey(key);
    result.ifPresent(data -> cache.put(key, data));
    return result;
  }

  @Override
  public void saveData(String key, String data) {
    dataService.saveData(key, data);
    cache.put(key, data);
  }

  @Override
  public boolean deleteData(String key) {
    cache.remove(key);
    return dataService.deleteData(key);
  }
}