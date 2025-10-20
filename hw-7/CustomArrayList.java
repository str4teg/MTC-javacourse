interface CustomList<A>{
  void add(A element);

  A get(int index);

  void remove(int index);

  int size();

  boolean isEmpty();
}

public class CustomArrayList<A> implements CustomList<A>{
  private Object[] list;

  public CustomArrayList()

}
