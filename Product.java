import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class Product extends AbstractExpression implements Expression {
    public final Expression left;
    public final Expression right;

    public Product(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public boolean equals(Object other) {
        if (other instanceof Product) {
            Product pOther = (Product) other;
            return left.equals(pOther.left) && right.equals(pOther.right);
        } else {
            return false;
        }
    }

    protected Expression substImpl(Expression haystack, Expression needle) {
        return new Product(left.substituteTerm(haystack, needle), right.substituteTerm(haystack, needle));
    }

    public String toString() {
        return "(" + left + " * " + right + ")";
    }

    public String toPolishString() {
        return "*" + left + right;
    }

    public int hashCode() {
        return toPolishString().hashCode();
    }
}
