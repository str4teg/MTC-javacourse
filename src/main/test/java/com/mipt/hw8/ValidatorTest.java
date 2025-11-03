package com.mipt.hw8;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class  ValidatorTest {

  public static class UserTestModel {
    @NotNull(message = "name null!")
    @Size(min = 2, max = 5, message = "name bad size!")
    private String name;

    @Range(min = 1, max = 120, message = "age bad range!")
    private Integer age;

    @Email(message = "email invalid!")
    private String email;

    public UserTestModel(String name, Integer age, String email) {
      this.name = name;
      this.age = age;
      this.email = email;
    }
  }


  // 1) success val
  @Test
  public void testValidUser() {
    UserTestModel u = new UserTestModel("Alex", 25, "test@gmail.com");
    ValidationResult r = Validator.validate(u);

    assertTrue(r.getValidity());
    assertEquals(0, r.getErrors().size());
  }

  // 2) null + size fail
  @Test
  public void testNullFieldFail() {
    UserTestModel u = new UserTestModel(null, 20, "test@gmail.com");
    ValidationResult r = Validator.validate(u);

    assertFalse(r.getValidity());
    assertTrue(r.getErrors().contains("name null!"));
  }

  // 3) range + email fail
  @Test
  public void testBadRangeAndEmail() {
    UserTestModel u = new UserTestModel("Bob", 3000, "wrong");
    ValidationResult r = Validator.validate(u);

    assertFalse(r.getValidity());
    assertTrue(r.getErrors().contains("age bad range!"));
    assertTrue(r.getErrors().contains("email invalid!"));
  }

  // 4) boundary cases size
  @Test
  public void testBoundarySize() {
    UserTestModel u = new UserTestModel("AB", 30, "valid@mail.com"); // min == 2 ok
    ValidationResult r = Validator.validate(u);

    assertTrue(r.getValidity());
    assertEquals(List.of(), r.getErrors());
  }
}
