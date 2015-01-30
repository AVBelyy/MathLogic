import java.util.Set;
import java.util.HashMap;
import java.util.List;

public class Pattern extends AbstractStatement implements Statement {
    // this mainly restricts how many "free" variables you may have in substitutions
    // like axioms and proof liberating and constructing rules.
    public static final int MAX_PATTERN_COUNT = 10;

    public final int patternId;

    public Pattern(int patternId) {
        this.patternId = patternId;
    }

    protected boolean compareImpl(String[] patterns, Statement other) {
        return other instanceof Pattern && patternId == ((Pattern) other).patternId;
    }

    public Statement substituteTerm(Expression haystack, Expression needle) {
        return this;
    }

    public Statement substitutePatterns(Statement[] to) {
        if (to[patternId] != null) {
            return to[patternId];
        } else {
            return this;
        }
    }

    public boolean estimate(HashMap<String, Boolean> values) {
        // undefined behavior
        return true;
    }

    public String toString() {
        return "$" + patternId;
    }

    public String toPolishString() {
        return toString();
    }
}
