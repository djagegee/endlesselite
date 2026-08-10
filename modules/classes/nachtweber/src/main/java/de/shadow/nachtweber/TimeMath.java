package de.shadow.nachtweber;

final class TimeMath {
  private TimeMath() { }
  static long saturatedAdd(long base,long positiveDelta) {
    if(base<0L||positiveDelta<0L) throw new IllegalArgumentException("time values must be non-negative");
    return base>Long.MAX_VALUE-positiveDelta?Long.MAX_VALUE:base+positiveDelta;
  }
}
