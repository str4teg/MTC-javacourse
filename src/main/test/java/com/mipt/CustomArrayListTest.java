package com.mipt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;

class CustomArrayListTest {

  private CustomList<String> customList;
  private CustomList<Integer> intList;

  @BeforeEach
  void setUp() {
    customList = new CustomArrayList<>();
    intList = new CustomArrayList<>();
  }

  @Test
  void testAddAndGet() {
    customList.add("first");
    customList.add("second");
    customList.add("third");

    assertEquals("first", customList.get(0));
    assertEquals("second", customList.get(1));
    assertEquals("third", customList.get(2));
  }

  @Test
  void testAddNullThrowsException() {
    assertThrows(NullPointerException.class, () -> {
      customList.add(null);
    });
  }

  @Test
  void testGetWithInvalidIndex() {
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
      customList.get(-1);
    });

     customList.add("element");
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
      customList.get(1);
    });

    assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
      customList.get(10);
    });
  }

  @Test
  void testSize() {
    assertEquals(0, customList.size());

    customList.add("one");
    assertEquals(1, customList.size());

    customList.add("two");
    assertEquals(2, customList.size());

    customList.add("three");
    assertEquals(3, customList.size());
  }

  @Test
  void testIsEmpty() {
    assertTrue(customList.isEmpty());

    customList.add("element");
    assertFalse(customList.isEmpty());

    customList.remove(0);
    assertTrue(customList.isEmpty());
  }

  @Test
  void testRemove() {
    customList.add("first");
    customList.add("second");
    customList.add("third");
    customList.add("fourth");

    String removed = customList.remove(1);
    assertEquals("second", removed);
    assertEquals(3, customList.size());
    assertEquals("first", customList.get(0));
    assertEquals("third", customList.get(1));
    assertEquals("fourth", customList.get(2));

    removed = customList.remove(0);
    assertEquals("first", removed);
    assertEquals(2, customList.size());
    assertEquals("third", customList.get(0));
    assertEquals("fourth", customList.get(1));

    removed = customList.remove(1);
    assertEquals("fourth", removed);
    assertEquals(1, customList.size());
    assertEquals("third", customList.get(0));
  }

  @Test
  void testRemoveWithInvalidIndex() {
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
      customList.remove(-1);
    });

     customList.add("element");
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
      customList.remove(1);
    });

    assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
      customList.remove(10);
    });
  }

  @Test
  void testDynamicResizing() {
    CustomArrayList<Integer> list = new CustomArrayList<>();

    for (int i = 0; i < 21; i++) {
      list.add(i);
    }

    assertEquals(21, list.size());

    for (int i = 0; i < 21; i++) {
      assertEquals(Integer.valueOf(i), list.get(i));
    }
  }

  @Test
  void testRemoveAllElements() {
    customList.add("one");
    customList.add("two");
    customList.add("three");

    customList.remove(0);
    customList.remove(0);
    customList.remove(0);

    assertEquals(0, customList.size());
    assertTrue(customList.isEmpty());
  }

  @Test
  void testIterator() {
    customList.add("first");
    customList.add("second");
    customList.add("third");

    Iterator<String> iterator = customList.iterator();

    assertTrue(iterator.hasNext());
    assertEquals("first", iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals("second", iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals("third", iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  void testIteratorOnEmptyList() {
    Iterator<String> iterator = customList.iterator();
    assertFalse(iterator.hasNext());
  }

  @Test
  void testIteratorNextOnEmptyListThrowsException() {
    Iterator<String> iterator = customList.iterator();
    assertThrows(ArrayIndexOutOfBoundsException.class, iterator::next);
  }

  @Test
  void testIteratorAfterRemoval() {
    customList.add("first");
    customList.add("second");
    customList.add("third");

    customList.remove(1);

    Iterator<String> iterator = customList.iterator();
    assertEquals("first", iterator.next());
    assertEquals("third", iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  void testForEachLoop() {
    customList.add("one");
    customList.add("two");
    customList.add("three");

    int count = 0;
    for (String element : customList) {
      assertNotNull(element);
      count++;
    }
    assertEquals(3, count);
  }

  @Test
  void testGenericTypes() {
    intList.add(1);
    intList.add(2);
    intList.add(3);

    assertEquals(Integer.valueOf(1), intList.get(0));
    assertEquals(Integer.valueOf(2), intList.get(1));
    assertEquals(Integer.valueOf(3), intList.get(2));

    Integer removed = intList.remove(1);
    assertEquals(Integer.valueOf(2), removed);
    assertEquals(2, intList.size());
  }

  @Test
  void testMultipleOperations() {
    assertTrue(customList.isEmpty());

    customList.add("start");
    assertFalse(customList.isEmpty());
    assertEquals(1, customList.size());

    customList.add("middle");
    assertEquals(2, customList.size());

    customList.add("end");
    assertEquals(3, customList.size());

    String removed = customList.remove(1);
    assertEquals("middle", removed);
    assertEquals(2, customList.size());

    customList.add("new");
    assertEquals(3, customList.size());

    assertEquals("start", customList.get(0));
    assertEquals("end", customList.get(1));
    assertEquals("new", customList.get(2));
  }

  @Test
  void testRemoveShiftsElementsCorrectly() {
    customList.add("A");
    customList.add("B");
    customList.add("C");
    customList.add("D");
    customList.add("E");

    customList.remove(2);

    assertEquals("A", customList.get(0));
    assertEquals("B", customList.get(1));
    assertEquals("D", customList.get(2));
    assertEquals("E", customList.get(3));
    assertEquals(4, customList.size());

    customList.remove(0);

    assertEquals("B", customList.get(0));
    assertEquals("D", customList.get(1));
    assertEquals("E", customList.get(2));
    assertEquals(3, customList.size());
  }
}