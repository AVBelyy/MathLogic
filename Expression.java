import java.util.Set;

interface Expression {
    Expression substituteTerm(Expression haystack, Expression needle);
    Set<String> getAllVariables();
    String toPolishString();
}
