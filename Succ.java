import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class Succ extends AbstractExpression implements Expression {
    public final Expression child;

    public Succ(Expression child) {
        this.child = child;
    }

    public boolean equals(Object other) {
        if (other instanceof Succ) {
            return child.equals(((Succ) other).child);
        } else {
            return false;
        }
    }

    protected Expression substImpl(Expression haystack, Expression needle) {
        return new Succ(child.substituteTerm(haystack, needle));
    }

    public String toString() {
        return "" + child + "'";
    }

    public String toPolishString() {
        return "'" + child;
    }

    public int hashCode() {
        return toPolishString().hashCode();
    }
}
