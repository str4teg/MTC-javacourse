package com.mipt;

import java.util.Iterator;

interface CustomList<A> extends Iterable<A>{
  void add(A element);

  A get(int index);

  A remove(int index);

  int size();

  boolean isEmpty();
}

public class CustomArrayList<A> implements CustomList<A>{
  private Object[] array;
  private int capacity;
  private int size;


  public CustomArrayList(){
    this.capacity = 20;
    this.array = new Object[capacity];
    this.size = 0;
  }

  @Override
  public void add (A element) {
    if (element == null){
      throw new NullPointerException("Can't add null");
    }
    if (size == capacity){
      capacity = (int) (capacity * 1.5);
      Object[] newArray = new Object[capacity];
      if (size >= 0) System.arraycopy(array, 0, newArray, 0, size);
      array = newArray;
    }
    array[size] = element;
    size += 1;
  }

  @Override
  public A get(int index){
    if (index < 0 || index >= size){
      throw new ArrayIndexOutOfBoundsException();
    }
    return (A) array[index];
  }

  @Override
  public A remove(int index){
    if (index < 0 || index >= size){
      throw new ArrayIndexOutOfBoundsException();
    }
    A removed = (A) array[index];
    for (int i = 0; i < size - index - 1; i++){
      array[index + i] = array[index + i + 1];
    }
    array [size] = null;
    size -= 1;

    return removed;
  }

  @Override
  public int size(){
    return size;
  }

  @Override
  public boolean isEmpty(){
    return size==0;
  }

  public Iterator<A> iterator() {
    return new CustomArrayListIterator();
  }

  private class CustomArrayListIterator implements Iterator<A> {

    private int currentIndex = 0;

    public boolean hasNext() {
      return currentIndex < size;
    }

    public A next() {
      if (!hasNext()) {
        throw new ArrayIndexOutOfBoundsException();
      }
      A elem = (A) array[currentIndex];
      currentIndex++;
      return elem;
    }
  }
}
