import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class Equals extends AbstractStatement implements Statement {
    public final Expression left;
    public final Expression right;

    public Equals(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    protected boolean compareImpl(String[] patterns, Statement other) {
        if (other instanceof Equals) {
            Equals eOther = (Equals) other;
            return left.equals(eOther.left) && right.equals(eOther.right);
        } else {
            return false;
        }
    }

    public Statement substituteTerm(Expression haystack, Expression needle) {
        return new Equals(left.substituteTerm(haystack, needle), right.substituteTerm(haystack, needle));
    }

    public Statement substitutePatterns(Statement[] to) {
        return this;
    }

    public boolean estimate(HashMap<String, Boolean> values) {
        // undefined for Equals
        return false;
    }

    public String toString() {
        return "(" + left + " = " + right + ")";
    }

    public String toPolishString() {
        return "=" + left + right;
    }
}
