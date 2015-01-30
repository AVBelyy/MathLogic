import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Forall extends AbstractStatement implements Statement {
    public final Statement child;
    public final String varName;

    public Forall(String varName, Statement child) {
        this.varName = varName;
        this.child = child;
    }

    protected boolean compareImpl(String[] patterns, Statement other) {
        if (other instanceof Forall && varName.equals(((Forall)other).varName)) {
            return child.compareWithinContext(patterns, ((Forall)other).child);
        } else {
            return false;
        }
    }

    public Statement substituteTerm(Expression haystack, Expression needle) {
        return new Forall(varName, child.substituteTerm(haystack, needle));
    }

    public Statement substitutePatterns(Statement[] to) {
        return new Forall(varName, child.substitutePatterns(to));
    }

    public boolean estimate(HashMap<String, Boolean> values) {
        // undefined in predicate calculus
        return false;
    }

    public String toString() {
        return "@" + varName + child;
    }
    
    public String toPolishString() {
        return "@" + varName + child.toPolishString();
    }
}
