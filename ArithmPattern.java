public class ArithmPattern extends AbstractExpression implements Expression {
    public int patternId;

    public ArithmPattern(int patternId) {
        this.patternId = patternId;
    }

    public boolean equals(Object other) {
        return other instanceof ArithmPattern && patternId == ((ArithmPattern) other).patternId;
    }

    protected Expression substImpl(Expression haystack, Expression needle) {
        return this;
    }

    public String toString() {
        return "#" + patternId;
    }

    public String toPolishString() {
        return toString();
    }

    public int hashCode() {
        return toPolishString().hashCode();
    }
}
