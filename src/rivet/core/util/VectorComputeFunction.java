package rivet.core.util;

@FunctionalInterface
public interface VectorComputeFunction {
    double apply(int k, double v);
}
