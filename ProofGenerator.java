import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

public class ProofGenerator {
    private static final ArrayList<Statement> doubleNegationProof = Helper.parseKnownLines(
        "$1",
        "$1->!$1->$1",
        "!$1->$1",
        "(!$1->$1)->(!$1->!$1)->!!$1",
        "(!$1->!$1)->!!$1",
        "!$1->!$1->!$1",
        "!$1->(!$1->!$1)->!$1",
        "(!$1->!$1->!$1)->(!$1->(!$1->!$1)->!$1)->(!$1->!$1)",
        "(!$1->(!$1->!$1)->!$1)->(!$1->!$1)",
        "!$1->!$1",
        "!!$1"
    );

    private static final ArrayList<Statement> disjunctionFFProof = Helper.parseKnownLines(
        "!$1",
        "!$2",
        "(($1|$2)->$1)->(($1|$2)->!$1)->!($1|$2)",
        "!$1->($1|$2)->!$1",
        "($1|$2)->!$1",
        "($1->$1)->($2->$1)->(($1|$2)->$1)",
        "$1->$1->$1",
        "$1->($1->$1)->$1",
        "($1->$1->$1)->($1->($1->$1)->$1)->($1->$1)",
        "($1->($1->$1)->$1)->($1->$1)",
        "$1->$1",
        "($2->$1)->(($1|$2)->$1)",
        "(!($2)->($2->!($2)))",
        "($2->!($2))",
        "($2->($2->$2))",
        "($2->(($2->$2)->$2))",
        "(($2->($2->$2))->(($2->(($2->$2)->$2))->($2->$2)))",
        "(($2->(($2->$2)->$2))->($2->$2))",
        "($2->$2)",
        "($2->(!($1)->$2))",
        "(($2->(!($1)->$2))->($2->($2->(!($1)->$2))))",
        "($2->($2->(!($1)->$2)))",
        "(($2->$2)->(($2->($2->(!($1)->$2)))->($2->(!($1)->$2))))",
        "(($2->($2->(!($1)->$2)))->($2->(!($1)->$2)))",
        "(!($2)->(!($1)->!($2)))",
        "((!($2)->(!($1)->!($2)))->($2->(!($2)->(!($1)->!($2)))))",
        "($2->(!($2)->(!($1)->!($2))))",
        "(($2->!($2))->(($2->(!($2)->(!($1)->!($2))))->($2->(!($1)->!($2)))))",
        "(($2->(!($2)->(!($1)->!($2))))->($2->(!($1)->!($2))))",
        "($2->(!($1)->!($2)))",
        "((!($1)->$2)->((!($1)->!($2))->!(!($1))))",
        "(((!($1)->$2)->((!($1)->!($2))->!(!($1))))->($2->((!($1)->$2)->((!($1)->!($2))->!(!($1))))))",
        "($2->((!($1)->$2)->((!($1)->!($2))->!(!($1)))))",
        "(($2->(!($1)->$2))->(($2->((!($1)->$2)->((!($1)->!($2))->!(!($1)))))->($2->((!($1)->!($2))->!(!($1))))))",
        "(($2->((!($1)->$2)->((!($1)->!($2))->!(!($1)))))->($2->((!($1)->!($2))->!(!($1)))))",
        "($2->((!($1)->!($2))->!(!($1))))",
        "(($2->(!($1)->!($2)))->(($2->((!($1)->!($2))->!(!($1))))->($2->!(!($1)))))",
        "(($2->((!($1)->!($2))->!(!($1))))->($2->!(!($1))))",
        "($2->!(!($1)))",
        "(!(!($1))->$1)",
        "((!(!($1))->$1)->($2->(!(!($1))->$1)))",
        "($2->(!(!($1))->$1))",
        "(($2->!(!($1)))->(($2->(!(!($1))->$1))->($2->$1)))",
        "(($2->(!(!($1))->$1))->($2->$1))",
        "($2->$1)",
        "($1|$2)->$1",
        "(($1|$2)->!$1)->!($1|$2)",
        "!($1|$2)"
    );
    private static final ArrayList<Statement> disjunction_TProof = Helper.parseKnownLines(
        "$2",
        "$2->$1|$2",
        "$1|$2"
    );
    private static final ArrayList<Statement> disjunctionT_Proof = Helper.parseKnownLines(
        "$1",
        "$1->$1|$2",
        "$1|$2"
    );

    private static final ArrayList<Statement> conjunctionF_Proof = Helper.parseKnownLines(
        "!$1",
        "$1&$2->$1",
        "(($1&$2)->$1)->(($1&$2)->!$1)->!($1&$2)",
        "(($1&$2)->!$1)->!($1&$2)",
        "!$1->($1&$2)->!$1",
        "($1&$2)->!$1",
        "!($1&$2)"
    );
    private static final ArrayList<Statement> conjunction_FProof = Helper.parseKnownLines(
        "!$2",
        "$1&$2->$2",
        "(($1&$2)->$2)->(($1&$2)->!$2)->!($1&$2)",
        "(($1&$2)->!$2)->!($1&$2)",
        "!$2->($1&$2)->!$2",
        "($1&$2)->!$2",
        "!($1&$2)"
    );
    private static final ArrayList<Statement> conjunctionTTProof = Helper.parseKnownLines(
        "$1",
        "$2",
        "$1->$2->$1&$2",
        "$2->$1&$2",
        "$1&$2"
    );

    private static final ArrayList<Statement> implicationFFProof = Helper.parseKnownLines(
        "!($1)",
        "(!($1)->($1->!($1)))",
        "($1->!($1))",
        "($1->($1->$1))",
        "($1->(($1->$1)->$1))",
        "(($1->($1->$1))->(($1->(($1->$1)->$1))->($1->$1)))",
        "(($1->(($1->$1)->$1))->($1->$1))",
        "($1->$1)",
        "($1->(!($2)->$1))",
        "(($1->(!($2)->$1))->($1->($1->(!($2)->$1))))",
        "($1->($1->(!($2)->$1)))",
        "(($1->$1)->(($1->($1->(!($2)->$1)))->($1->(!($2)->$1))))",
        "(($1->($1->(!($2)->$1)))->($1->(!($2)->$1)))",
        "(!($1)->(!($2)->!($1)))",
        "((!($1)->(!($2)->!($1)))->($1->(!($1)->(!($2)->!($1)))))",
        "($1->(!($1)->(!($2)->!($1))))",
        "(($1->!($1))->(($1->(!($1)->(!($2)->!($1))))->($1->(!($2)->!($1)))))",
        "(($1->(!($1)->(!($2)->!($1))))->($1->(!($2)->!($1))))",
        "($1->(!($2)->!($1)))",
        "((!($2)->$1)->((!($2)->!($1))->!(!($2))))",
        "(((!($2)->$1)->((!($2)->!($1))->!(!($2))))->($1->((!($2)->$1)->((!($2)->!($1))->!(!($2))))))",
        "($1->((!($2)->$1)->((!($2)->!($1))->!(!($2)))))",
        "(($1->(!($2)->$1))->(($1->((!($2)->$1)->((!($2)->!($1))->!(!($2)))))->($1->((!($2)->!($1))->!(!($2))))))",
        "(($1->((!($2)->$1)->((!($2)->!($1))->!(!($2)))))->($1->((!($2)->!($1))->!(!($2)))))",
        "($1->((!($2)->!($1))->!(!($2))))",
        "(($1->(!($2)->!($1)))->(($1->((!($2)->!($1))->!(!($2))))->($1->!(!($2)))))",
        "(($1->((!($2)->!($1))->!(!($2))))->($1->!(!($2))))",
        "($1->!(!($2)))",
        "(!(!($2))->$2)",
        "((!(!($2))->$2)->($1->(!(!($2))->$2)))",
        "($1->(!(!($2))->$2))",
        "(($1->!(!($2)))->(($1->(!(!($2))->$2))->($1->$2)))",
        "(($1->(!(!($2))->$2))->($1->$2))",
        "($1->$2)"
    );
    private static final ArrayList<Statement> implication_TProof = Helper.parseKnownLines(
        "$2",
        "$2->$1->$2",
        "$1->$2"
    );
    private static final ArrayList<Statement> implicationTFProof = Helper.parseKnownLines(
        "(($1->$2)->$2)->(($1->$2)->!$2)->!($1->$2)",
        "$1",
        "$1->($1->$2)->$1",
        "($1->$2)->$1",
        "($1->$2)->($1->$2)->($1->$2)",
        "(($1->$2)->($1->$2)->($1->$2))->(($1->$2)->(($1->$2)->($1->$2))->($1->$2))->(($1->$2)->($1->$2))",
        "(($1->$2)->(($1->$2)->($1->$2))->($1->$2))->(($1->$2)->($1->$2))",
        "($1->$2)->(($1->$2)->($1->$2))->($1->$2)",
        "($1->$2)->($1->$2)",
        "(($1->$2)->$1)->(($1->$2)->($1->$2))->(($1->$2)->$2)",
        "(($1->$2)->($1->$2))->(($1->$2)->$2)",
        "($1->$2)->$2",
        "!$2",
        "!$2->($1->$2)->!$2",
        "($1->$2)->!$2",
        "(($1->$2)->!$2)->!($1->$2)",
        "!($1->$2)"
    );

    private static final ArrayList<Statement> contrapositionProof = Helper.parseKnownLines(
        "(($1->$2)->(!($2)->($1->$2)))",
        "(($1->$2)->(($1->!($2))->!($1)))",
        "((($1->$2)->(($1->!($2))->!($1)))->(($1->$2)->(($1->$2)->(($1->!($2))->!($1)))))",
        "(($1->$2)->(($1->$2)->(($1->!($2))->!($1))))",
        "((($1->$2)->(($1->!($2))->!($1)))->(!($2)->(($1->$2)->(($1->!($2))->!($1)))))",
        "(((($1->$2)->(($1->!($2))->!($1)))->(!($2)->(($1->$2)->(($1->!($2))->!($1)))))->(($1->$2)->((($1->$2)->(($1->!($2))->!($1)))->(!($2)->(($1->$2)->(($1->!($2))->!($1)))))))",
        "(($1->$2)->((($1->$2)->(($1->!($2))->!($1)))->(!($2)->(($1->$2)->(($1->!($2))->!($1))))))",
        "((($1->$2)->(($1->$2)->(($1->!($2))->!($1))))->((($1->$2)->((($1->$2)->(($1->!($2))->!($1)))->(!($2)->(($1->$2)->(($1->!($2))->!($1))))))->(($1->$2)->(!($2)->(($1->$2)->(($1->!($2))->!($1)))))))",
        "((($1->$2)->((($1->$2)->(($1->!($2))->!($1)))->(!($2)->(($1->$2)->(($1->!($2))->!($1))))))->(($1->$2)->(!($2)->(($1->$2)->(($1->!($2))->!($1))))))",
        "(($1->$2)->(!($2)->(($1->$2)->(($1->!($2))->!($1)))))",
        "((!($2)->($1->$2))->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1)))))",
        "(((!($2)->($1->$2))->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1)))))->(($1->$2)->((!($2)->($1->$2))->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1)))))))",
        "(($1->$2)->((!($2)->($1->$2))->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1))))))",
        "((($1->$2)->(!($2)->($1->$2)))->((($1->$2)->((!($2)->($1->$2))->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1))))))->(($1->$2)->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1)))))))",
        "((($1->$2)->((!($2)->($1->$2))->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1))))))->(($1->$2)->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1))))))",
        "(($1->$2)->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1)))))",
        "((($1->$2)->(!($2)->(($1->$2)->(($1->!($2))->!($1)))))->((($1->$2)->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1)))))->(($1->$2)->(!($2)->(($1->!($2))->!($1))))))",
        "((($1->$2)->((!($2)->(($1->$2)->(($1->!($2))->!($1))))->(!($2)->(($1->!($2))->!($1)))))->(($1->$2)->(!($2)->(($1->!($2))->!($1)))))",
        "(($1->$2)->(!($2)->(($1->!($2))->!($1))))",
        "(!($2)->($1->!($2)))",
        "((!($2)->($1->!($2)))->(($1->$2)->(!($2)->($1->!($2)))))",
        "(($1->$2)->(!($2)->($1->!($2))))",
        "((!($2)->($1->!($2)))->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1))))",
        "(((!($2)->($1->!($2)))->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1))))->(($1->$2)->((!($2)->($1->!($2)))->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1))))))",
        "(($1->$2)->((!($2)->($1->!($2)))->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1)))))",
        "((($1->$2)->(!($2)->($1->!($2))))->((($1->$2)->((!($2)->($1->!($2)))->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1)))))->(($1->$2)->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1))))))",
        "((($1->$2)->((!($2)->($1->!($2)))->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1)))))->(($1->$2)->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1)))))",
        "(($1->$2)->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1))))",
        "((($1->$2)->(!($2)->(($1->!($2))->!($1))))->((($1->$2)->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1))))->(($1->$2)->(!($2)->!($1)))))",
        "((($1->$2)->((!($2)->(($1->!($2))->!($1)))->(!($2)->!($1))))->(($1->$2)->(!($2)->!($1))))",
        "(($1->$2)->(!($2)->!($1)))"
    );

    // unfortunately, this proof is too big to be written manually
    private static final ArrayList<Statement> tertiumNonDaturProof;

    private static final ArrayList<Statement> varEliminationProof = Helper.parseKnownLines(
        "($1->$2)->(!$1->$2)->($1|!$1->$2)",
        "(!$1->$2)->($1|!$1->$2)",
        "$1|!$1->$2",
        "$2"
    );

    static {
        // prove Tertium Non Datur
        ArrayList<Statement> tndBegin = Helper.parseKnownLines(
            "$1->$1|!$1",
            "!$1->$1|!$1"
        );
        ArrayList<Statement> tnd1Cpos = generateAtomicProof(contrapositionProof, Helper.parseKnownLine("$1"), Helper.parseKnownLine("$1|!$1"));
        ArrayList<Statement> tnd2Cpos = generateAtomicProof(contrapositionProof, Helper.parseKnownLine("!$1"), Helper.parseKnownLine("$1|!$1"));
        ArrayList<Statement> tndEnd = Helper.parseKnownLines(
            "!($1|!$1)->!$1",
            "!($1|!$1)->!!$1",
            "(!($1|!$1)->!$1)->(!($1|!$1)->!!$1)->!!($1|!$1)",
            "(!($1|!$1)->!!$1)->!!($1|!$1)",
            "!!($1|!$1)",
            "!!($1|!$1)->($1|!$1)",
            "$1|!$1"
        );
        ArrayList<Statement> tndProof = new ArrayList<>();
        tndProof.addAll(tndBegin);
        tndProof.addAll(tnd1Cpos);
        tndProof.addAll(tnd2Cpos);
        tndProof.addAll(tndEnd);
        tertiumNonDaturProof = Helper.optimize(tndProof);
    }

    private static ArrayList<Statement> generateAtomicProof(ArrayList<Statement> atomicProof, Statement... substitution) {
        ArrayList<Statement> genProof = new ArrayList<>(atomicProof.size());

        for (Statement proofLine : atomicProof) {
            genProof.add(proofLine.substitutePatterns(substitution));
        }

        return genProof;
    }

    private ArrayList<Statement> context = new ArrayList<>();
    private HashSet<String> hashedContext = new HashSet<>();
    private HashMap<String, Boolean> values = new HashMap<>();
    private Statement inStatement;
    private ArrayList<String> variables;

    public ProofGenerator(Statement inStatement) {
        this.inStatement = inStatement;
        this.variables = new ArrayList<String>(Helper.getVariables(inStatement));
    }

    private ArrayList<Statement> generateInContext(Statement statement) throws GeneratorException {
        if (statement instanceof BinaryOperation) {
            Statement left = ((BinaryOperation) statement).left;
            Statement right = ((BinaryOperation) statement).right;
            boolean leftValue = left.estimate(values);
            boolean rightValue = right.estimate(values);

            if (statement instanceof Conjunction) {
                if (leftValue && rightValue) {
                    ArrayList<Statement> proof1 = generateInContext(left);
                    ArrayList<Statement> proof2 = generateInContext(right);
                    ArrayList<Statement> proof3 = generateAtomicProof(conjunctionTTProof, left, right);

                    proof1.addAll(proof2);
                    proof1.addAll(proof3);
                    return proof1;
                } else {
                    throw new GeneratorException(values);
                }
            } else if (statement instanceof Disjunction) {
                if (leftValue) {
                    ArrayList<Statement> proof1 = generateInContext(left);
                    ArrayList<Statement> proof2 = generateAtomicProof(disjunctionT_Proof, left, right);

                    proof1.addAll(proof2);
                    return proof1;
                } else if (rightValue) {
                    ArrayList<Statement> proof1 = generateInContext(right);
                    ArrayList<Statement> proof2 = generateAtomicProof(disjunction_TProof, left, right);

                    proof1.addAll(proof2);
                    return proof1;
                } else {
                    throw new GeneratorException(values);
                }
            } else if (statement instanceof Implication) {
                if (!leftValue && !rightValue) {
                    ArrayList<Statement> proof1 = generateInContext(new Negation(left));
                    ArrayList<Statement> proof2 = generateInContext(new Negation(right));
                    ArrayList<Statement> proof3 = generateAtomicProof(implicationFFProof, left, right);

                    proof1.addAll(proof2);
                    proof1.addAll(proof3);
                    return proof1;
                } else if (rightValue) {
                    ArrayList<Statement> proof1 = generateInContext(right);
                    ArrayList<Statement> proof2 = generateAtomicProof(implication_TProof, left, right);

                    proof1.addAll(proof2);
                    return proof1;
                } else {
                    throw new GeneratorException(values);
                }
            } else {
                throw new GeneratorException(values);
            }
        } else if (statement instanceof Negation && ((Negation) statement).child instanceof BinaryOperation) {
            Statement negStatement = ((Negation) statement).child;
            Statement left = ((BinaryOperation) negStatement).left;
            Statement right = ((BinaryOperation) negStatement).right;
            boolean leftValue = left.estimate(values);
            boolean rightValue = right.estimate(values);
            
            if (negStatement instanceof Conjunction) {
                if (!leftValue) {
                    ArrayList<Statement> proof1 = generateInContext(new Negation(left));
                    ArrayList<Statement> proof2 = generateAtomicProof(conjunctionF_Proof, left, right);

                    proof1.addAll(proof2);
                    return proof1;
                } else if (!rightValue) {
                    ArrayList<Statement> proof1 = generateInContext(new Negation(right));
                    ArrayList<Statement> proof2 = generateAtomicProof(conjunction_FProof, left, right);

                    proof1.addAll(proof2);
                    return proof1;
                } else {
                    throw new GeneratorException(values);
                }
            } else if (negStatement instanceof Disjunction) {
                if (!leftValue && !rightValue) {
                    ArrayList<Statement> proof1 = generateInContext(new Negation(left));
                    ArrayList<Statement> proof2 = generateInContext(new Negation(right));
                    ArrayList<Statement> proof3 = generateAtomicProof(disjunctionFFProof, left, right);

                    proof1.addAll(proof2);
                    proof1.addAll(proof3);
                    return proof1;
                } else {
                    throw new GeneratorException(values);
                }
            } else if (negStatement instanceof Implication) {
                if (leftValue && !rightValue) {
                    ArrayList<Statement> proof1 = generateInContext(left);
                    ArrayList<Statement> proof2 = generateInContext(new Negation(right));
                    ArrayList<Statement> proof3 = generateAtomicProof(implicationTFProof, left, right);

                    proof1.addAll(proof2);
                    proof1.addAll(proof3);
                    return proof1;
                } else {
                    throw new GeneratorException(values);
                }
            } else {
                throw new GeneratorException(values);
            }
        } else if (statement instanceof Negation && ((Negation) statement).child instanceof Negation) {
            Statement grandchild = ((Negation) (((Negation) statement).child)).child;
            ArrayList<Statement> proof1 = generateInContext(grandchild);
            ArrayList<Statement> proof2 = generateAtomicProof(doubleNegationProof, grandchild);
            
            proof1.addAll(proof2);
            return proof1;
        } else if (hashedContext.contains(statement.toPolishString())) {
            ArrayList<Statement> genProof = new ArrayList<>();
            
            genProof.add(statement);
            return genProof;
        } else {
            throw new GeneratorException(values);
        }
    }

    private ArrayList<Statement> generateOne(int step) throws GeneratorException {
        if (step == variables.size()) {
            return generateInContext(inStatement);
        } else {
            try {
                String varName = variables.get(step);
                Statement variable = new Variable(varName);

                context.add(new Negation(variable));
                hashedContext.add("!" + varName);
                values.put(varName, false);
                ArrayList<Statement> stmtsA = generateOne(step + 1);
                Proof proofA = new Proof(context, Helper.annotateInContext(context, stmtsA));
                ArrayList<Statement> liberatedProofA = Helper.removeAnnotation(proofA.liberate().optimize().getStatements());
                hashedContext.remove("!" + varName);
                context.remove(step);

                context.add(variable);
                hashedContext.add(varName);
                values.put(varName, true);
                ArrayList<Statement> stmtsNotA = generateOne(step + 1);
                Proof proofNotA = new Proof(context, Helper.annotateInContext(context, stmtsNotA));
                ArrayList<Statement> liberatedProofNotA = Helper.removeAnnotation(proofNotA.liberate().optimize().getStatements());
                hashedContext.remove(varName);
                context.remove(step);

                liberatedProofA.addAll(liberatedProofNotA);
                liberatedProofA.addAll(generateAtomicProof(tertiumNonDaturProof, variable));
                liberatedProofA.addAll(generateAtomicProof(varEliminationProof, variable, inStatement));
                
                return liberatedProofA;
            } catch (AnnotatorException e) {
                // unreachable state, if my proofs are correct
                System.err.println("Error while annotating " + e.getStatement());
                throw new GeneratorException(values);
            }
        }
    }

    public ArrayList<Statement> generate() throws GeneratorException {
        return generateOne(0);
    }
}
