import java.util.Set;
import java.util.List;
import java.util.HashMap;

public class Variable extends AbstractStatement implements Statement {
    public final String name;

    public Variable(String name) {
        this.name = name;
    }

    protected boolean compareImpl(String[] patterns, Statement other) {
        if (other instanceof Variable) {
            return name.equals(((Variable)other).name);
        } else {
            return false;
        }
    }

    public Statement substituteTerm(Expression haystack, Expression needle) {
        return this;
    }

    public Statement substitutePatterns(Statement[] to) {
        return this;
    }

    public boolean estimate(HashMap<String, Boolean> values) {
        // invariant: variable is present in values map
        return values.get(name);
    }

    public String toString() {
        return name;
    }

    public String toPolishString() {
        return name;
    }
}
