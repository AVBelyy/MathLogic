import java.util.List;
import java.util.Set;
import java.util.HashSet;

public abstract class AbstractExpression implements Expression {
    public Expression substituteTerm(Expression haystack, Expression needle) {
        if (equals(haystack)) {
            return needle;
        } else {
            return substImpl(haystack, needle);
        }
    }

    public Set<String> getAllVariables() {
        Set<String> vars = new HashSet<>();

        if (this instanceof Function) {
            Function ex = (Function) this;
            if (ex.terms.size() == 0) {
                vars.add(ex.name);
            } else {
                for (Expression term : ex.terms) {
                    vars.addAll(term.getAllVariables());
                }
            }
        } else if (this instanceof Product) {
            Product ex = (Product) this;
            vars.addAll(ex.left.getAllVariables());
            vars.addAll(ex.right.getAllVariables());
        } else if (this instanceof Sum) {
            Sum ex = (Sum) this;
            vars.addAll(ex.left.getAllVariables());
            vars.addAll(ex.right.getAllVariables());
        } else if (this instanceof Succ) {
            Succ ex = (Succ) this;
            vars.addAll(ex.child.getAllVariables());
        }

        return vars;
    }

    protected abstract Expression substImpl(Expression haystack, Expression needle);
}
