import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Homework1 {
    public static void main(String[] args) {
        String testFilename = args[0];

        PredicateParser parser = new PredicateParser();

        ArrayList<Statement> context = new ArrayList<>();
        ArrayList<ProofAnnotator.AnnotatedStatement> statements = new ArrayList<>();
        
        try {
            // parse proof 
            BufferedReader br = new BufferedReader(new FileReader(testFilename));
            String line;

            while ((line = br.readLine()) != null) {
                Statement stmt = parser.parse(line);
                statements.add(new ProofAnnotator.Unannotated(stmt));
            }

            // combine context and statements together
            Proof proof = new Proof(new ArrayList<>(), statements);

            // annotate proof
            Proof annotatedProof = proof.annotate();
            ArrayList<ProofAnnotator.AnnotatedStatement> proofStatements = annotatedProof.getStatements();
            
            for (int i = 0; i < proofStatements.size(); i++) {
                ProofAnnotator.AnnotatedStatement annStmt = proofStatements.get(i);
                System.out.print("" + (i + 1) + ") " + annStmt.statement + " ");
                if (annStmt instanceof ProofAnnotator.Hypothesis) {
                    System.out.print("(гип. " + (((ProofAnnotator.Hypothesis)annStmt).lineNo + 1) + ")");
                } else if (annStmt instanceof ProofAnnotator.Axiom) {
                    System.out.print("(акс. " + (((ProofAnnotator.Axiom)annStmt).axiomId + 1) + ")");
                }
                if (annStmt instanceof ProofAnnotator.ModusPonens) {
                    ProofAnnotator.ModusPonens mpStmt = (ProofAnnotator.ModusPonens)annStmt;
                    System.out.print("(m.p. " + (mpStmt.alpha + 1) + ", " + (mpStmt.beta + 1) + ")");
                }
                System.out.println();
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
