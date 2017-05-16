package rivet.core.lexicon;

import java.util.function.UnaryOperator;

import org.apache.commons.lang3.tuple.MutablePair;

import rivet.core.labels.RIV;

public class LexiconEntry extends MutablePair<RIV, RIV> {
  /**
  * 
  */
  private static final long serialVersionUID = 7118580821717702017L;

  public LexiconEntry(final RIV lex, final RIV ind) {
    super(lex, ind);
  }

  public RIV lex() {
    return right;
  }

  public RIV ind() {
    return left;
  }

  public LexiconEntry mapRight(final UnaryOperator<RIV> fun) {
    return new LexiconEntry(left, fun.apply(right));
  }
}
