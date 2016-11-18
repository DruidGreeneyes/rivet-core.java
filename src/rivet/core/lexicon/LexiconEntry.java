package rivet.core.lexicon;

import java.util.function.UnaryOperator;

import druid.utils.pair.UniformPair;
import rivet.core.labels.RIV;

public class LexiconEntry extends UniformPair<RIV> {
    public LexiconEntry(final RIV lex, final RIV ind) {
        super(lex, ind);
    }

    public RIV lex() {
        return right;
    }

    public RIV ind() {
        return left;
    }

    @Override
    public LexiconEntry mapRight(final UnaryOperator<RIV> fun) {
        return new LexiconEntry(left, fun.apply(right));
    }
}
