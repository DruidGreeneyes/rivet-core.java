package com.github.druidgreeneyes.rivet.core.labels;

@FunctionalInterface
public interface RIVConstructor {
  public RIV make(final int[] keys, final double[] vals, final int size);
}
