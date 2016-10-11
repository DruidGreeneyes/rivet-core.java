package rivet.core.lexicon;

import java.util.function.UnaryOperator;

import pair.UniformPair;
import rivet.core.labels.MapRIV;

public class LexiconEntry extends UniformPair<MapRIV> {
    public LexiconEntry(MapRIV lex, MapRIV ind) {
        super(lex, ind);
    }

    public MapRIV lex() {
        return right;
    }

    public MapRIV ind() {
        return left;
    }

    public LexiconEntry mapRight(UnaryOperator<MapRIV> fun) {
        return new LexiconEntry(left, fun.apply(right));
    }
}
