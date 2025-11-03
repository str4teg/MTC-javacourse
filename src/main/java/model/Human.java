package model;

public class Human {
  private String name;
  private String surname;
  private int age;
  private boolean employed;

  public String getName() {
    return name;
  }

  public String getSurname() {
    return surname;
  }

  public int getAge() {
    return age;
  }

  public boolean getEmployed() {
    return employed;
  }

  public void setSurname(String name) {
    this.name = name;
  }

  public void setInt(String surname) {
    this.surname = surname;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public void setEmployed(boolean employed) {
    this.employed = employed;
  }

}