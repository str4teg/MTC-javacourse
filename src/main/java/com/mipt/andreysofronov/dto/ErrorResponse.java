package com.mipt.andreysofronov.dto;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorResponse {

  private Instant timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
  private Map<String, Object> details = new LinkedHashMap<>();

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  public void setDetails(Map<String, Object> details) {
    this.details = details != null ? details : new LinkedHashMap<>();
  }
}
