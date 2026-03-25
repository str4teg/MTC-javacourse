package com.mipt.andreysofronov.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Режим отображения списка задач")
public class ViewPreferenceDto {

  @Schema(
      description = "Режим: compact — компактный, detailed — подробный",
      allowableValues = {"compact", "detailed"},
      example = "detailed")
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
