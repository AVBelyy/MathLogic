import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Predicate extends AbstractStatement implements Statement {
    public final String name;
    public final ArrayList<Expression> terms;

    public Predicate(String name, ArrayList<Expression> terms) {
        this.name = name;
        this.terms = terms;
    }

    protected boolean compareImpl(String[] patterns, Statement other) {
        if (other instanceof Predicate) {
            Predicate pOther = (Predicate) other;
            if (name.equals(pOther.name) && terms.size() == pOther.terms.size()) {
                for (int i = 0; i < terms.size(); i++) {
                    if (!terms.get(i).equals(pOther.terms.get(i))) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public Statement substituteTerm(Expression haystack, Expression needle) {
        ArrayList<Expression> newTerms = new ArrayList<>();

        for (Expression term : terms) {
            newTerms.add(term.substituteTerm(haystack, needle));
        }

        return new Predicate(name, newTerms);
    }

    public Statement substitutePatterns(Statement[] to) {
        return this;
    }

    public boolean estimate(HashMap<String, Boolean> values) {
        // undefined for Predicate
        return false;
    }

    public String toString() {
        String str = name;
        if (terms.size() != 0) {
            str += "(";
            for (int i = 0; i < terms.size(); i++) {
                str += terms.get(i);
                if (i != terms.size() - 1) {
                    str += ", ";
                }
            }
            str += ")";
        }
        return str;
    }

    public String toPolishString() {
        return toString();
    }
}
