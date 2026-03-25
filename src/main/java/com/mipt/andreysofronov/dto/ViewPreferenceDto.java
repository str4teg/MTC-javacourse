package com.mipt.andreysofronov.dto;

public class ViewPreferenceDto {

  private String mode;

  public ViewPreferenceDto() {}

  public ViewPreferenceDto(String mode) {
    this.mode = mode;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }
}
