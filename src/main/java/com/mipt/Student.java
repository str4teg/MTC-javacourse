package com.mipt;

import java.util.*;
import java.util.stream.Collectors;

class Student {
  private int id;
  private String name;
  private double grade;

  public Student(int id, String name, double grade) {
    this.id = id;
    this.name = name;
    this.grade = grade;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Student student = (Student) o;
    return id == student.id && Double.compare(student.grade, grade) == 0 && Objects.equals(name, student.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, grade);
  }

  public static HashMap<Integer, Student> createHashMap() {
    HashMap<Integer, Student> map = new HashMap<>();
    map.put(1, new Student(1, "Alice", 4.5));
    map.put(2, new Student(2, "Bob", 3.8));
    map.put(3, new Student(3, "Charlie", 4.2));
    map.put(4, new Student(4, "Diana", 3.5));
    map.put(5, new Student(5, "Eve", 4.8));
    return map;
  }

  public static TreeMap<Integer, Student> createTreeMap() {
    TreeMap<Integer, Student> map = new TreeMap<>(Collections.reverseOrder());
    map.put(1, new Student(1, "Alice", 4.5));
    map.put(2, new Student(2, "Bob", 3.8));
    map.put(3, new Student(3, "Charlie", 4.2));
    map.put(4, new Student(4, "Diana", 3.5));
    map.put(5, new Student(5, "Eve", 4.8));
    return map;
  }

  public static List<Student> findStudentsByGradeRange(Map<Integer, Student> map, double minGrade, double maxGrade) {
    return map.values().stream()
        .filter(student -> student.grade >= minGrade && student.grade <= maxGrade)
        .collect(Collectors.toList());
  }

  public static List<Student> getTopNStudents(TreeMap<Integer, Student> map, int n) {
    return map.descendingMap().values().stream()
        .limit(n)
        .collect(Collectors.toList());
  }
}