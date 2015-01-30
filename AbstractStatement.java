import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public abstract class AbstractStatement implements Statement {
    private static boolean comparePatternToStatement(String[] patterns, Pattern a, Statement s) {
        int patternId = a.patternId;
        String sStr = s.toString();

        if (patterns[patternId] == null) {
            patterns[patternId] = sStr;
            return true;
        } else if (patterns[patternId].equals(sStr)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean compareWithinContext(String[] patterns, Statement other) {
        if (other == null) {
            return false;
        }

        if (this instanceof Pattern) {
            return comparePatternToStatement(patterns, (Pattern)this, other);
        } else if (other instanceof Pattern) {
            return comparePatternToStatement(patterns, (Pattern)other, this);
        } else {
            return compareImpl(patterns, other);
        }
    }

    public boolean equals(Object other) {
        if (other instanceof AbstractStatement) {
            String[] patterns = new String[Pattern.MAX_PATTERN_COUNT];

            return compareWithinContext(patterns, (Statement)other);
        } else {
            return false;
        }
    }

    public Set<String> getFreeVariables() {
        return freeImpl(this, new ArrayList<>());
    }

    public static Set<String> freeImpl(Statement stmt, List<String> bound) {
        Set<String> vars = new HashSet<>();

        if (stmt instanceof BinaryOperation) {
            BinaryOperation st = (BinaryOperation) stmt;
            vars.addAll(freeImpl(st.left, bound));
            vars.addAll(freeImpl(st.right, bound));
        } else if (stmt instanceof Negation) {
            Negation st = (Negation) stmt;
            vars.addAll(freeImpl(st.child, bound));
        } else if (stmt instanceof Exists) {
            Exists st = (Exists) stmt;
            List<String> newBound = new ArrayList<String>(bound);
            newBound.add(st.varName);
            vars.addAll(freeImpl(st.child, newBound));
        } else if (stmt instanceof Forall) {
            Forall st = (Forall) stmt;
            List<String> newBound = new ArrayList<String>(bound);
            newBound.add(st.varName);
            vars.addAll(freeImpl(st.child, newBound));
        } else if (stmt instanceof Equals) {
            Equals st = (Equals) stmt;
            vars.addAll(freeImpl(st.left, bound));
            vars.addAll(freeImpl(st.right, bound));
        } else if (stmt instanceof Predicate) {
            Predicate st = (Predicate) stmt;
            for (Expression term : st.terms) {
                vars.addAll(freeImpl(term, bound));
            }
        }
        
        return vars;
    }

    public static Set<String> freeImpl(Expression expr, List<String> bound) {
        Set<String> vars = new HashSet<>();

        if (expr instanceof Function) {
            Function ex = (Function) expr;
            if (ex.terms.size() == 0) {
                if (!bound.contains(ex.name)) {
                    vars.add(ex.name);
                }
            } else {
                for (Expression term : ex.terms) {
                    vars.addAll(freeImpl(term, bound));
                }
            }
        } else if (expr instanceof Product) {
            Product ex = (Product) expr;
            vars.addAll(freeImpl(ex.left, bound));
            vars.addAll(freeImpl(ex.right, bound));
        } else if (expr instanceof Sum) {
            Sum ex = (Sum) expr;
            vars.addAll(freeImpl(ex.left, bound));
            vars.addAll(freeImpl(ex.right, bound));
        } else if (expr instanceof Succ) {
            Succ ex = (Succ) expr;
            vars.addAll(freeImpl(ex.child, bound));
        }

        return vars;
    }

    public Set<String> getAllVariables() {
        Set<String> vars = new HashSet<>();

        if (this instanceof BinaryOperation) {
            BinaryOperation st = (BinaryOperation) this;
            vars.addAll(st.left.getAllVariables());
            vars.addAll(st.right.getAllVariables());
        } else if (this instanceof Negation) {
            Negation st = (Negation) this;
            vars.addAll(st.child.getAllVariables());
        } else if (this instanceof Exists) {
            Exists st = (Exists) this;
            vars.addAll(st.child.getAllVariables());
        } else if (this instanceof Forall) {
            Forall st = (Forall) this;
            vars.addAll(st.child.getAllVariables());
        } else if (this instanceof Equals) {
            Equals st = (Equals) this;
            vars.addAll(st.left.getAllVariables());
            vars.addAll(st.right.getAllVariables());
        } else if (this instanceof Predicate) {
            Predicate st = (Predicate) this;
            for (Expression term : st.terms) {
                vars.addAll(term.getAllVariables());
            }
        }
        
        return vars;
    }

    public Set<String> getBoundVariables() {
        Set<String> vars = new HashSet<>();

        if (this instanceof BinaryOperation) {
            BinaryOperation st = (BinaryOperation) this;
            vars.addAll(st.left.getBoundVariables());
            vars.addAll(st.right.getBoundVariables());
        } else if (this instanceof Negation) {
            Negation st = (Negation) this;
            vars.addAll(st.child.getBoundVariables());
        } else if (this instanceof Exists) {
            Exists st = (Exists) this;
            vars.addAll(st.child.getBoundVariables());
            vars.add(st.varName);
        } else if (this instanceof Forall) {
            Forall st = (Forall) this;
            vars.addAll(st.child.getBoundVariables());
            vars.add(st.varName);
        }
        
        return vars;
    }

    public boolean freeForSubstitution(String x, Expression theta) {
        Set<String> thetaVars = theta.getAllVariables();
        Function var = new Function(x, new ArrayList<>());
        Set<String> freeVars = substituteTerm(var, theta).getFreeVariables();
        return freeVars.containsAll(thetaVars);
    }

    public int hashCode() {
        return toPolishString().hashCode();
    }

    protected abstract boolean compareImpl(String[] patterns, Statement other);
}
