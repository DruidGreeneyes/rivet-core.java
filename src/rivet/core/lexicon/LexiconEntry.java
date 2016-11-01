package rivet.core.lexicon;

import java.util.function.UnaryOperator;

import druid.utils.pair.UniformPair;
import rivet.core.labels.MapRIV;

public class LexiconEntry extends UniformPair<MapRIV> {
    public LexiconEntry(final MapRIV lex, final MapRIV ind) {
        super(lex, ind);
    }

    public MapRIV lex() {
        return right;
    }

    public MapRIV ind() {
        return left;
    }

    @Override
    public LexiconEntry mapRight(final UnaryOperator<MapRIV> fun) {
        return new LexiconEntry(left, fun.apply(right));
    }
}
