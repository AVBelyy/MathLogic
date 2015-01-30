import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class Sum extends AbstractExpression implements Expression {
    public final Expression left;
    public final Expression right;

    public Sum(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public boolean equals(Object other) {
        if (other instanceof Sum) {
            Sum sOther = (Sum) other;
            return left.equals(sOther.left) && right.equals(sOther.right);
        } else {
            return false;
        }
    }

    protected Expression substImpl(Expression haystack, Expression needle) {
        return new Sum(left.substituteTerm(haystack, needle), right.substituteTerm(haystack, needle));
    }

    public String toString() {
        return "(" + left + " + " + right + ")";
    }

    public String toPolishString() {
        return "+" + left + right;
    }

    public int hashCode() {
        return toPolishString().hashCode();
    }
}
