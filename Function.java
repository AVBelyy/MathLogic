import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

public class Function extends AbstractExpression implements Expression {
    public final String name;
    public final ArrayList<Expression> terms;

    public Function(String name, ArrayList<Expression> terms) {
        this.name = name;
        this.terms = terms;
    }

    public boolean equals(Object other) {
        if (other instanceof Function) {
            Function fOther = (Function) other;
            if (name.equals(fOther.name) && terms.size() == fOther.terms.size()) {
                for (int i = 0; i < terms.size(); i++) {
                    if (!terms.get(i).equals(fOther.terms.get(i))) {
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
    
    protected Expression substImpl(Expression haystack, Expression needle) {
        ArrayList<Expression> newTerms = new ArrayList<>();

        for (Expression term : terms) {
            newTerms.add(term.substituteTerm(haystack, needle));
        }

        return new Function(name, newTerms);
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

    public int hashCode() {
        return toPolishString().hashCode();
    }
}
