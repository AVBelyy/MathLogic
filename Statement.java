import java.util.HashMap;
import java.util.Set;
import java.util.List;

interface Statement {
    boolean compareWithinContext(String[] patterns, Statement other);
    Statement substitutePatterns(Statement[] to);
    Statement substituteTerm(Expression haystack, Expression needle);
    Set<String> getFreeVariables();
    Set<String> getAllVariables();
    Set<String> getBoundVariables();
    boolean freeForSubstitution(String x, Expression phi);
    boolean estimate(HashMap<String, Boolean> values);
    String toPolishString();
}
