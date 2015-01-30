import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Negation extends AbstractStatement implements Statement {
    public final Statement child;

    public Negation(Statement child) {
        this.child = child;
    }

    protected boolean compareImpl(String[] patterns, Statement other) {
        if (other instanceof Negation) {
            return child.compareWithinContext(patterns, ((Negation)other).child);
        } else {
            return false;
        }
    }

    public Statement substitutePatterns(Statement[] to) {
        return new Negation(child.substitutePatterns(to));
    }

    public Statement substituteTerm(Expression haystack, Expression needle) {
        return new Negation(child.substituteTerm(haystack, needle));
    }

    public boolean estimate(HashMap<String, Boolean> values) {
        return !child.estimate(values);
    }

    public String toString() {
        return "!(" + child + ")";
    }
    
    public String toPolishString() {
        return "!" + child.toPolishString();
    }
}
