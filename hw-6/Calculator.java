public class Calculator<T extends Number> {
  public double sum(T a, T b) {
    return a == null || b == null ? Double.NaN : a.doubleValue() + b.doubleValue();
  }

  public double subtract(T a, T b) {
    return a == null || b == null ? Double.NaN : a.doubleValue() - b.doubleValue();
  }

  public double multiply(T a, T b) {
    return a == null || b == null ? Double.NaN : a.doubleValue() * b.doubleValue();
  }

  public double divide(T a, T b) {
    return a == null || b == null || b.doubleValue() == 0 ? Double.NaN : a.doubleValue() / b.doubleValue();
  }
}
