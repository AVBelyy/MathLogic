import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Homework2 {
    public static void main(String[] args) {
        String testFilename = args[0];

        PredicateParser parser = new PredicateParser();

        ArrayList<Statement> context = new ArrayList<>();
        ArrayList<ProofAnnotator.AnnotatedStatement> statements = new ArrayList<>();
        
        try {
            // parse proof 
            // firstly, parse context
            BufferedReader br = new BufferedReader(new FileReader(testFilename));
            String line = br.readLine();

            String[] headStr = line.split("\\|\\-");
            String[] contextStr = headStr[0].split(",");

            Statement expectedConclusion = parser.parse(headStr[1]);
            Set<String> freeCtxVars = new HashSet<>();

            for (int i = 0; i < contextStr.length; i++) {
                try {
                    Statement stmt = parser.parse(contextStr[i]);
                    context.add(stmt);
                    if (i == contextStr.length - 1) {
                        freeCtxVars.addAll(stmt.getFreeVariables());
                    }
                    statements.add(new ProofAnnotator.Hypothesis(stmt, i));
                } catch (ParserException ignore) {
                }
            }
            
            // secondly, parse statements
            while ((line = br.readLine()) != null) {
                Statement stmt = parser.parse(line);
                statements.add(new ProofAnnotator.Unannotated(stmt));
            }

            // combine context and statements together
            Proof proof = new Proof(context, statements);

            // liberate and annotate proof
            Proof annotatedLiberatedProof = proof.liberate().optimize().annotate();
            ArrayList<ProofAnnotator.AnnotatedStatement> proofStatements = annotatedLiberatedProof.getStatements();

            String errorFmt = "Вывод некорректен начиная с формулы номер %d: %s\n";

            Statement actualConclusion = statements.get(statements.size() - 1).statement;
            if (!actualConclusion.equals(expectedConclusion)) {
                System.err.println("Пришли к неожиданному выводу. Ожидалось " + expectedConclusion + ", но получили " + actualConclusion);
            } else {
                for (int i = 0; i < proofStatements.size(); i++) {
                    ProofAnnotator.AnnotatedStatement annStmt = proofStatements.get(i);
                    Statement stmt = annStmt.statement;
                    Statement toCheck = null;
                    boolean isAxiom = true;

                    if (annStmt instanceof ProofAnnotator.Axiom) {
                        if (((ProofAnnotator.Axiom) annStmt).axiomId == 10) {
                            toCheck = ((Implication) stmt).left;
                        } else if (((ProofAnnotator.Axiom) annStmt).axiomId == 11) {
                            toCheck = ((Implication) stmt).right;
                        }
                    } else if (annStmt instanceof ProofAnnotator.InferenceRule) {
                        if (((ProofAnnotator.InferenceRule) annStmt).n == 1) {
                            toCheck = ((Implication) stmt).right;
                        } else if (((ProofAnnotator.InferenceRule) annStmt).n == 2) {
                            toCheck = ((Implication) stmt).left;
                        }
                        isAxiom = false;
                    }

                    if (toCheck != null) {
                        Set<String> boundVars = toCheck.getBoundVariables();
                        for (String var : freeCtxVars) {
                            if (boundVars.contains(var) && !isAxiom) {
                                System.out.format(errorFmt, i + 1, "Используется правило с квантором по переменной " + var + ", входящей свободно в допущение " + context.get(context.size() - 1));
                                System.exit(1);
                            }
                        }
                    }
                }

                for (int i = 0; i < proofStatements.size(); i++) {
                    ProofAnnotator.AnnotatedStatement annStmt = proofStatements.get(i);
                    Statement stmt = annStmt.statement;
                    System.out.print("" + (i + 1) + ") " + stmt + " ");
                    if (annStmt instanceof ProofAnnotator.Hypothesis) {
                        System.out.print("(гип. " + (((ProofAnnotator.Hypothesis)annStmt).lineNo + 1) + ")");
                    } else if (annStmt instanceof ProofAnnotator.Axiom) {
                        System.out.print("(акс. " + (((ProofAnnotator.Axiom)annStmt).axiomId + 1) + ")");
                    }
                    if (annStmt instanceof ProofAnnotator.ModusPonens) {
                        ProofAnnotator.ModusPonens mpStmt = (ProofAnnotator.ModusPonens)annStmt;
                        System.out.print("(m.p. " + (mpStmt.alpha + 1) + ", " + (mpStmt.beta + 1) + ")");
                    }
                    if (annStmt instanceof ProofAnnotator.InferenceRule) {
                        ProofAnnotator.InferenceRule irStmt = (ProofAnnotator.InferenceRule)annStmt;
                        System.out.print("(п.в. " + irStmt.n + ", " + (irStmt.lineNo + 1) + ")");
                    }
                    System.out.println();
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        } catch (ParserException e) {
            System.err.println("Ошибка при парсинге " + e.getLine());
        } catch (AnnotatorException e) {
            System.err.println("Ошибка при аннотации " + e.getStatement() + ": " + e.getMessage());
        }
    }
}
