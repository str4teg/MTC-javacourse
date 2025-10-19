import java.util.*;

public class CollectionUtils {
  public static <T> List<T> mergeLists(List<? extends T> list1,
                                       List<? extends T> list2) {
    List<T> mergedList = new ArrayList<>();

    if (list1 == null && list2 == null) {
      return null;
    }

    if (list1 != null) {
      mergedList.addAll(list1);
    }

    if (list2 != null) {
      mergedList.addAll(list2);
    }

    return mergedList;
  }

  public static <T> void addAll(List<? super T> destination,
                                List<? extends T> source) {
    if (destination != null && source != null) {
      destination.addAll(source);
    }
  }
}