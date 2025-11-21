package com.mipt.hw8;

import java.util.ArrayList;

public class ValidationResult {
  private boolean isValid;
  private ArrayList<String>  errors;

  public ValidationResult(){
    this.isValid = true;
    this.errors = new ArrayList<>();
  }

  public boolean getValidity(){
    return isValid;
  }

  public ArrayList<String> getErrors(){
    return errors;
  }

  public void setValidity(boolean newValidity) {
    isValid = newValidity;
  }

  public void addError(String error){
    errors.add(error);
    isValid = false;
  }
}