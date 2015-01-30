import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Helper {
    public static ArrayList<Statement> axioms = parseKnownLines(
        "$1->$2->$1",
        "($1->$2)->($1->$2->$3)->($1->$3)",
        "$1->$2->$1&$2",
        "$1&$2->$1",
        "$1&$2->$2",
        "$1->$1|$2",
        "$2->$1|$2",
        "($1->$3)->($2->$3)->($1|$2->$3)",
        "($1->$2)->($1->!$2)->!$1",
        "!!$1->$1"
    );

    public static ArrayList<Statement> arithmAxioms = parseKnownLines(
        "#1=#2->#1'=#2'",
        "#1=#2->#1=#3->#2=#3",
        "#1'=#2'->#1=#2",
        "!#1'=0",
        "#1+#2'=(#1+#2)'",
        "#1+0=#1",
        "#1*0=0",
        "#1*#2'=#1*#2+#1"
    );

    public static ArrayList<Statement> parseLines(String... lines) throws ParserException {
        PredicateParser parser = new PredicateParser();
        ArrayList<Statement> statements = new ArrayList<Statement>(lines.length);

        for (String line : lines) {
            Statement stmt = parser.parse(line);
            statements.add(stmt);
        }

        return statements;
    }

    public static ArrayList<Statement> parseKnownLines(String... lines) {
        try {
            return parseLines(lines);
        } catch (ParserException ignore) {
            // this is unreachable
            return null;
        }
    }

    public static Statement parseKnownLine(String line) {
        return parseKnownLines(line).get(0);
    }

    public static ArrayList<ProofAnnotator.AnnotatedStatement> dummyAnnotate(ArrayList<Statement> statements) {
        ArrayList<ProofAnnotator.AnnotatedStatement> annStmts = new ArrayList<>();

        for (Statement stmt : statements) {
            annStmts.add(new ProofAnnotator.Unannotated(stmt));
        }

        return annStmts;
    }

    public static ArrayList<Statement> optimize(ArrayList<Statement> statements) {
        LinkedHashSet<Statement> listToSet = new LinkedHashSet<>(statements);
        return new ArrayList<>(listToSet);
    }

    private static void optimizeRecursively(boolean[] used, ArrayList<ProofAnnotator.AnnotatedStatement> statements, int current) {
        ProofAnnotator.AnnotatedStatement annStatement = statements.get(current);
        
        used[current] = true;

        if (annStatement instanceof ProofAnnotator.ModusPonens) {
            int alpha = ((ProofAnnotator.ModusPonens) annStatement).alpha;
            int beta = ((ProofAnnotator.ModusPonens) annStatement).beta;
            
            used[alpha] = true;
            used[beta] = true;
            optimizeRecursively(used, statements, alpha);
            optimizeRecursively(used, statements, beta);
        } else if (annStatement instanceof ProofAnnotator.InferenceRule) {
            int lineNo = ((ProofAnnotator.InferenceRule) annStatement).lineNo;

            used[lineNo] = true;
            optimizeRecursively(used, statements, lineNo);
        }
    }

    public static ArrayList<Statement> optimizeAnnotated(ArrayList<ProofAnnotator.AnnotatedStatement> statements) {
        ArrayList<Statement> optStatements = new ArrayList<>();
        boolean[] used = new boolean[statements.size()];

        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof ProofAnnotator.Unannotated) {
                used[i] = true;
            }
        }

        optimizeRecursively(used, statements, statements.size() - 1);

        for (int i = 0; i < statements.size(); i++) {
            if (used[i]) {
                optStatements.add(statements.get(i).statement);
            }
        }

        return optStatements;
    }

    public static HashSet<String> getVariables(Statement statement) {
        if (statement instanceof BinaryOperation) {
            Statement left = ((BinaryOperation) statement).left;
            Statement right = ((BinaryOperation) statement).right;
            HashSet<String> leftVariables = getVariables(left);
            HashSet<String> rightVariables = getVariables(right);

            leftVariables.addAll(rightVariables);
            return leftVariables;
        } else if (statement instanceof Negation) {
            return getVariables(((Negation) statement).child);
        } else if (statement instanceof Variable) {
            HashSet<String> variables = new HashSet<>();

            variables.add(((Variable) statement).name);
            return variables;
        } else {
            return new HashSet<>();
        }
    }

    public static ArrayList<ProofAnnotator.AnnotatedStatement> annotateInContext(ArrayList<Statement> context, ArrayList<Statement> statements) {
        ArrayList<ProofAnnotator.AnnotatedStatement> annStatements = new ArrayList<>(statements.size());

        for (Statement stmt : statements) {
            int hypothesisNum = -1;

            for (int i = 0; i < context.size(); i++) {
                if (stmt.equals(context.get(i))) {
                    hypothesisNum = i;
                    break;
                }
            }

            if (hypothesisNum != -1) {
                annStatements.add(new ProofAnnotator.Hypothesis(stmt, hypothesisNum));
            } else {
                annStatements.add(new ProofAnnotator.Unannotated(stmt));
            }
        }

        return annStatements;
    }

    public static ArrayList<Statement> removeAnnotation(ArrayList<ProofAnnotator.AnnotatedStatement> annStatements) {
        ArrayList<Statement> statements = new ArrayList<>(annStatements.size());

        for (ProofAnnotator.AnnotatedStatement annStmt : annStatements) {
            statements.add(annStmt.statement);
        }

        return statements;
    }
}
