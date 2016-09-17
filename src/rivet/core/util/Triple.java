package rivet.core.util;

import java.util.function.Function;

public class Triple<A, B, C> {
    public final A left;
    public final B center;
    public final C right;

    private Triple(A a, B b, C c) {
        left = a;
        center = b;
        right = c;
    }

    public final A left() {
        return left;
    }

    public final B center() {
        return center;
    }

    public final C right() {
        return right;
    }

    public <R> Triple<R, B, C> mapLeft(Function<A, R> fun) {
        return Triple.make(fun.apply(left), center, right);
    }

    public <R> Triple<A, R, C> mapCenter(Function<B, R> fun) {
        return Triple.make(left, fun.apply(center), right);
    }

    public <R> Triple<A, B, R> mapRight(Function<C, R> fun) {
        return Triple.make(left, center, fun.apply(right));
    }

    public static <A, B, C> Triple<A, B, C> make(A left, B center, C right) {
        return new Triple<>(left, center, right);
    }
}
