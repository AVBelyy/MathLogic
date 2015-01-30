import java.util.ArrayList;
import java.util.HashMap;

public class ProofAnnotator {
    public static abstract class AnnotatedStatement {
        public Statement statement;

        public boolean equals(Object other) {
            if (this instanceof Unannotated && other instanceof Unannotated) {
                return statement.equals(((AnnotatedStatement) other).statement);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return statement.hashCode();
        }

        protected AnnotatedStatement(Statement statement) {
            this.statement = statement;
        }
    }

    public static class Unannotated extends AnnotatedStatement {
        public Unannotated(Statement statement) {
            super(statement);
        }
    }

    public static class Hypothesis extends AnnotatedStatement {
        public int lineNo;

        public Hypothesis(Statement statement, int lineNo) {
            super(statement);

            this.lineNo = lineNo;
        }
    }

    public static class Axiom extends AnnotatedStatement {
        public int axiomId;

        public Axiom(Statement statement, int axiomId) {
            super(statement);

            this.axiomId = axiomId;
        }
    }

    public static class ModusPonens extends AnnotatedStatement {
        public int alpha;
        public int beta;

        public ModusPonens(Statement statement, int alpha, int beta) {
            super(statement);

            this.alpha = alpha;
            this.beta = beta;
        }
    }

    public static class InferenceRule extends AnnotatedStatement {
        public int n;
        public int lineNo;

        public InferenceRule(Statement statement, int n, int lineNo) {
            super(statement);

            this.n = n;
            this.lineNo = lineNo;
        }
    }

    private static class MPPair {
        private int alpha;
        private int beta;

        public MPPair(int alpha, int beta) {
            this.alpha = alpha;
            this.beta = beta;
        }
    }

    public static ArrayList<AnnotatedStatement> annotate(ArrayList<Statement> context, ArrayList<AnnotatedStatement> proof) throws AnnotatorException {
        HashMap<String, Integer> ctxLines = new HashMap<>();
        HashMap<String, Integer> lines = new HashMap<>();
        HashMap<String, Integer> needAlpha = new HashMap<>();
        HashMap<String, MPPair> betas = new HashMap<>();

        ArrayList<AnnotatedStatement> annotatedProof = new ArrayList<>();

        for (int lineNo = 0; lineNo < context.size(); lineNo++) {
            Statement hyp = context.get(lineNo);
            String hypStr = hyp.toPolishString();

            ctxLines.put(hypStr, lineNo);
        }

        for (int lineNo = 0; lineNo < proof.size(); lineNo++) {
            AnnotatedStatement annStmt = proof.get(lineNo);
            Statement stmt = annStmt.statement;
            String stmtStr = stmt.toPolishString();

            if (stmt instanceof Implication) {
                // add to potential modus ponens-able list
                String alphaStr = ((Implication)stmt).left.toPolishString();
                String betaStr = ((Implication)stmt).right.toPolishString();

                // find potential alpha above alpha->beta
                if (lines.containsKey(alphaStr)) {
                    betas.put(betaStr, new MPPair(lines.get(alphaStr), lineNo));
                }
                // if not found, maybe it's somewhere below?
                else {
                    needAlpha.put(alphaStr, lineNo);
                }
            }

            // check if some statement needs us
            if (needAlpha.containsKey(stmtStr)) {
                int alphaBetaNo = needAlpha.get(stmtStr);
                Statement beta = ((Implication)proof.get(alphaBetaNo).statement).right;
                String betaStr = beta.toPolishString();
                betas.put(betaStr, new MPPair(lineNo, alphaBetaNo));
                needAlpha.remove(stmtStr);
            }
            
            // add to lines list
            if (!lines.containsKey(stmtStr)) {
                lines.put(stmtStr, lineNo);
            }

            // if current line is a hypothesis
            if (annStmt instanceof Hypothesis) {
                annotatedProof.add(annStmt);
            } else {
                // check if a statement is the axiom 1-10
                boolean isAxiom = false;

                for (int i = 0; i < Helper.axioms.size(); i++) {
                    if (stmt.equals(Helper.axioms.get(i))) {
                        annotatedProof.add(new Axiom(stmt, i));
                        isAxiom = true;
                        break;
                    }
                }

                if (isAxiom) {
                    continue;
                }

                // check if a statement is the arithmetic axiom 1-8

                for (int i = 0; i < Helper.arithmAxioms.size(); i++) {
                    PatternMatcher patternMatcher = new PatternMatcher(true);
                    patternMatcher.match(Helper.arithmAxioms.get(i), stmt);
                    if (patternMatcher.isValid()) {
                        annotatedProof.add(new Axiom(stmt, i));
                        isAxiom = true;
                        break;
                    }
                }

                if (isAxiom) {
                    continue;
                }

                // check if a statement is the axiom 11-12 or the inference rule 1-2 of Predicate Calculus
                if (stmt instanceof Implication) {
                    Statement stL = ((Implication) stmt).left;
                    Statement stR = ((Implication) stmt).right;

                    // (φ) → (ψ) ⇒ (φ) → ∀x(ψ)
                    if (stR instanceof Forall) {
                        String varName = ((Forall) stR).varName;
                        Statement subR = ((Forall) stR).child;

                        String toSearch = new Implication(stL, subR).toPolishString();

                        if (lines.containsKey(toSearch)) {
                            if (stL.getFreeVariables().contains(varName)) {
                                throw new AnnotatorException(stmt, "переменная " + varName + " входит свободно в формулу " + stL);
                            } else {
                                // statement is the inference rule 1
                                int bareLineNo = lines.get(toSearch);
                                annotatedProof.add(new InferenceRule(stmt, 1, bareLineNo));
                                continue;
                            }
                        }
                    }

                    // (ψ) → (φ) ⇒ ∃x(ψ) → (φ)
                    if (stL instanceof Exists) {
                        String varName = ((Exists) stL).varName;
                        Statement subL = ((Exists) stL).child;

                        String toSearch = new Implication(subL, stR).toPolishString();

                        if (lines.containsKey(toSearch)) {
                            if (stR.getFreeVariables().contains(varName)) {
                                throw new AnnotatorException(stmt, "переменная " + varName + " входит свободно в формулу " + stR);
                            } else {
                                // statement is the inference rule 2
                                int bareLineNo = lines.get(toSearch);
                                annotatedProof.add(new InferenceRule(stmt, 2, bareLineNo));
                                continue;
                            }
                        }
                    }

                    // ∀x(ψ) → (ψ[x := θ])
                    if (stL instanceof Forall) {
                        String varName = ((Forall) stL).varName;
                        Statement subQ = ((Forall) stL).child;

                        Function var = new Function(varName, new ArrayList<>());
                        PatternMatcher patternMatcher = new PatternMatcher(false);
                        
                        patternMatcher.match(subQ.substituteTerm(var, new ArithmPattern(0)), stR);
                        Expression theta = patternMatcher.matched[0];

                        if (theta != null) {
                            if (subQ.freeForSubstitution(varName, theta)) {
                                // statement is the axiom 11
                                annotatedProof.add(new Axiom(stmt, 10));
                                continue;
                            } else {
                                throw new AnnotatorException(stmt, "терм " + theta + " не свободен для подстановки в формулу " + subQ + " вместо переменной " + varName);
                            }
                        }
                    }

                    // (ψ[x := θ]) → ∃x(ψ)
                    if (stR instanceof Exists) {
                        String varName = ((Exists) stR).varName;
                        Statement subQ = ((Exists) stR).child;

                        Function var = new Function(varName, new ArrayList<>());
                        PatternMatcher patternMatcher = new PatternMatcher(false);
                        
                        patternMatcher.match(subQ.substituteTerm(var, new ArithmPattern(0)), stL);
                        Expression theta = patternMatcher.matched[0];

                        if (theta != null) {
                            if (subQ.freeForSubstitution(varName, theta)) {
                                // statement is the axiom 12
                                annotatedProof.add(new Axiom(stmt, 11));
                                continue;
                            } else {
                                throw new AnnotatorException(stmt, "терм " + theta + " не свободен для подстановки в формулу " + subQ + " вместо переменной " + varName);
                            }
                        }
                    }

                    // (ψ[x := 0]) & ∀x((ψ) → (ψ)[x := x']) → (ψ)
                    if (stL instanceof Conjunction) {
                        Statement stLL = ((Conjunction) stL).left;
                        Statement stLR = ((Conjunction) stL).right;

                        if (stLR instanceof Forall) {
                            String varName = ((Forall) stLR).varName;
                            Statement subQ = ((Forall) stLR).child;
                            Function var = new Function(varName, new ArrayList<>());

                            if (subQ instanceof Implication) {
                                Statement subQL = ((Implication) subQ).left;
                                Statement subQR = ((Implication) subQ).right;

                                boolean flag = subQL.equals(stR)
                                    && stLL.equals(stR.substituteTerm(var, new Zero()))
                                    && subQR.equals(stR.substituteTerm(var, new Succ(var)));

                                if (flag) {
                                    // statement is the axiom A9
                                    annotatedProof.add(new Axiom(stmt, 8));
                                    continue;
                                }
                            }
                        }
                    }
                }

                // check if an expression is a modus ponens or hypothesis
                if (betas.containsKey(stmtStr)) {
                    MPPair mp = betas.get(stmtStr);
                    annotatedProof.add(new ModusPonens(stmt, mp.alpha, mp.beta));
                } else if (ctxLines.containsKey(stmtStr)) {
                    annotatedProof.add(new Hypothesis(stmt, ctxLines.get(stmtStr)));
                } else {
                    // error while annotating
                    throw new AnnotatorException(stmt, "неизвестное выражение");
                }
            }
        }

        return annotatedProof;
    }
    
    static class PatternMatcher {
        public Expression matched[];
        private int numOfVars;
        private boolean validState;
        private boolean strictMode;

        public PatternMatcher(boolean strictMode) {
            this.numOfVars = 0;
            this.matched = new Expression[10];
            this.validState = true;
            this.strictMode = strictMode;
        }

        public boolean isValid() {
            if (!validState || numOfVars == 0) {
                return false;
            }
            for (int i = 0; i < numOfVars; i++) {
                if (matched[i] == null) {
                    return false;
                }
            }
            return true;
        }

        // Macaroni below

        private void match(Expression a, Expression b) {
            if (a instanceof ArithmPattern) {
                int patternId = ((ArithmPattern) a).patternId;
                numOfVars = Math.max(numOfVars, patternId + 1);
                if (matched[patternId] != null && !matched[patternId].equals(b)) {
                    validState = false;
                } else if (!strictMode || (b instanceof Function && ((Function) b).terms.size() == 0)) {
                    matched[patternId] = b;
                }
            } else if (b instanceof ArithmPattern) {
                int patternId = ((ArithmPattern) b).patternId;
                numOfVars = Math.max(numOfVars, patternId + 1);
                if (matched[patternId] != null && !matched[patternId].equals(a)) {
                    validState = false;
                } else if (!strictMode || (a instanceof Function && ((Function) a).terms.size() == 0)) {
                    matched[patternId] = a;
                }
            } else if (a instanceof Function && b instanceof Function) {
                Function stA = (Function) a;
                Function stB = (Function) b;
                if (stA.terms.size() == stB.terms.size()) {
                    for (int i = 0; i < stA.terms.size(); i++) {
                        match(stA.terms.get(i), stB.terms.get(i));
                    }
                }
            } else if (a instanceof Product && b instanceof Product) {
                Product stA = (Product) a;
                Product stB = (Product) b;
                match(stA.left, stB.left);
                match(stA.right, stB.right);
            } else if (a instanceof Sum && b instanceof Sum) {
                Sum stA = (Sum) a;
                Sum stB = (Sum) b;
                match(stA.left, stB.left);
                match(stA.right, stB.right);
            } else if (a instanceof Succ && b instanceof Succ) {
                Succ stA = (Succ) a;
                Succ stB = (Succ) b;
                match(stA.child, stB.child);
            } else if (a instanceof Zero && b instanceof Zero) {
                // ok
            } else {
                validState = false;
            }
        }

        private void match(Statement a, Statement b) {
            if (a instanceof BinaryOperation && b instanceof BinaryOperation) {
                BinaryOperation stA = (BinaryOperation) a;
                BinaryOperation stB = (BinaryOperation) b;
                if (stA.opCode == stB.opCode) {
                    match(stA.left, stB.left);
                    match(stA.right, stB.right);
                }
            } else if (a instanceof Negation && b instanceof Negation) {
                Negation stA = (Negation) a;
                Negation stB = (Negation) b;
                match(stA.child, stB.child);
            } else if (a instanceof Exists && b instanceof Exists) {
                Exists stA = (Exists) a;
                Exists stB = (Exists) b;
                match(stA.child, stB.child);
            } else if (a instanceof Forall && b instanceof Forall) {
                Forall stA = (Forall) a;
                Forall stB = (Forall) b;
                match(stA.child, stB.child);
            } else if (a instanceof Equals && b instanceof Equals) {
                Equals stA = (Equals) a;
                Equals stB = (Equals) b;
                match(stA.left, stB.left);
                match(stA.right, stB.right);
            } else if (a instanceof Predicate && b instanceof Predicate) {
                Predicate stA = (Predicate) a;
                Predicate stB = (Predicate) b;
                if (stA.terms.size() == stB.terms.size()) {
                    for (int i = 0; i < stA.terms.size(); i++) {
                        match(stA.terms.get(i), stB.terms.get(i));
                    }
                }
            } else {
                validState = false;
            }
        }
    }
}
