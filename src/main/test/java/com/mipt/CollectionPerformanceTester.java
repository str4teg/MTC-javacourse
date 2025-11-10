package com.mipt;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class CollectionPerformanceTester {

  private static final int ELEMENT_COUNT = 10000;

  @Test
  void testArrayListPerformance() {
    System.out.println("Производительность ArrayList для " + ELEMENT_COUNT + " элементов");
    System.out.println("==================================================");
    System.out.printf("%-25s | %-15s%n", "Операция", "Время (мс)");
    System.out.println("--------------------------------------------------");

    testArrayListAddToEnd();
    testArrayListAddToBeginning();
    testArrayListInsertInMiddle();
    testArrayListRandomAccess();
    testArrayListRemoveFromBeginning();
    testArrayListRemoveFromEnd();

    System.out.println("==================================================");
  }

  @Test
  void testLinkedListPerformance() {
    System.out.println("Производительность LinkedList для " + ELEMENT_COUNT + " элементов");
    System.out.println("==================================================");
    System.out.printf("%-25s | %-15s%n", "Операция", "Время (мс)");
    System.out.println("--------------------------------------------------");

    testLinkedListAddToEnd();
    testLinkedListAddToBeginning();
    testLinkedListInsertInMiddle();
    testLinkedListRandomAccess();
    testLinkedListRemoveFromBeginning();
    testLinkedListRemoveFromEnd();

    System.out.println("==================================================");
  }

  private void testArrayListAddToEnd() {
    List<Integer> arrayList = new ArrayList<>();

    long time = measureTime(() -> {
      for (int i = 0; i < ELEMENT_COUNT; i++) {
        arrayList.add(i);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Добавление в конец", time);
  }

  private void testArrayListAddToBeginning() {
    List<Integer> arrayList = new ArrayList<>();

    long time = measureTime(() -> {
      for (int i = 0; i < ELEMENT_COUNT; i++) {
        arrayList.add(0, i);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Добавление в начало", time);
  }

  private void testArrayListInsertInMiddle() {
    List<Integer> arrayList = new ArrayList<>();

    for (int i = 0; i < ELEMENT_COUNT; i++) {
      arrayList.add(i);
    }

    long time = measureTime(() -> {
      for (int i = 0; i < 1000; i++) {
        int middleIndex = arrayList.size() / 2;
        arrayList.add(middleIndex, i);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Вставка в середину", time);
  }

  private void testArrayListRandomAccess() {
    List<Integer> arrayList = new ArrayList<>();

    for (int i = 0; i < ELEMENT_COUNT; i++) {
      arrayList.add(i);
    }

    long time = measureTime(() -> {
      for (int i = 0; i < ELEMENT_COUNT; i++) {
        int element = arrayList.get(i);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Доступ по индексу", time);
  }

  private void testArrayListRemoveFromBeginning() {
    List<Integer> arrayList = new ArrayList<>();

    for (int i = 0; i < ELEMENT_COUNT; i++) {
      arrayList.add(i);
    }

    long time = measureTime(() -> {
      while (!arrayList.isEmpty()) {
        arrayList.remove(0);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Удаление из начала", time);
  }

  private void testArrayListRemoveFromEnd() {
    List<Integer> arrayList = new ArrayList<>();

    for (int i = 0; i < ELEMENT_COUNT; i++) {
      arrayList.add(i);
    }

    long time = measureTime(() -> {
      while (!arrayList.isEmpty()) {
        arrayList.remove(arrayList.size() - 1);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Удаление из конца", time);
  }

  private void testLinkedListAddToEnd() {
    List<Integer> linkedList = new LinkedList<>();

    long time = measureTime(() -> {
      for (int i = 0; i < ELEMENT_COUNT; i++) {
        linkedList.add(i);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Добавление в конец", time);
  }

  private void testLinkedListAddToBeginning() {
    List<Integer> linkedList = new LinkedList<>();

    long time = measureTime(() -> {
      for (int i = 0; i < ELEMENT_COUNT; i++) {
        linkedList.add(0, i);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Добавление в начало", time);
  }

  private void testLinkedListInsertInMiddle() {
    List<Integer> linkedList = new LinkedList<>();

    for (int i = 0; i < ELEMENT_COUNT; i++) {
      linkedList.add(i);
    }

    long time = measureTime(() -> {
      for (int i = 0; i < 1000; i++) {
        int middleIndex = linkedList.size() / 2;
        linkedList.add(middleIndex, i);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Вставка в середину", time);
  }

  private void testLinkedListRandomAccess() {
    List<Integer> linkedList = new LinkedList<>();

    for (int i = 0; i < ELEMENT_COUNT; i++) {
      linkedList.add(i);
    }

    long time = measureTime(() -> {
      for (int i = 0; i < ELEMENT_COUNT; i++) {
        int element = linkedList.get(i);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Доступ по индексу", time);
  }

  private void testLinkedListRemoveFromBeginning() {
    List<Integer> linkedList = new LinkedList<>();

    for (int i = 0; i < ELEMENT_COUNT; i++) {
      linkedList.add(i);
    }

    long time = measureTime(() -> {
      while (!linkedList.isEmpty()) {
        linkedList.remove(0);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Удаление из начала", time);
  }

  private void testLinkedListRemoveFromEnd() {
    List<Integer> linkedList = new LinkedList<>();

    for (int i = 0; i < ELEMENT_COUNT; i++) {
      linkedList.add(i);
    }

    long time = measureTime(() -> {
      while (!linkedList.isEmpty()) {
        linkedList.remove(linkedList.size() - 1);
      }
    });

    System.out.printf("%-25s | %-15d%n", "Удаление из конца", time);
  }

  private long measureTime(Runnable operation) {
    long startTime = System.nanoTime();
    operation.run();
    long endTime = System.nanoTime();
    return (endTime - startTime) / 1_000_000;
  }
}