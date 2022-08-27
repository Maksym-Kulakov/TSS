package de.jade.ecs.map.shipchart;

public class PairHash {
  private final Integer hash;

  public PairHash(Integer a, Integer b) {
    Integer sum = a + b;
    hash = sum * (sum + 1) / 2 + a;
  }

  public int hashCode() {
    return hash;
  }
}
