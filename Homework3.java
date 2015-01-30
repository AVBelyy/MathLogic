import java.util.ArrayList;
import java.util.Map.Entry;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Homework3 {
    public static void main(String[] args) {
        String testFilename = args[0];

        PropositionalParser parser = new PropositionalParser();

        ArrayList<Statement> axioms = new ArrayList<>();
        ArrayList<Statement> context = new ArrayList<>();
        ArrayList<ProofAnnotator.AnnotatedStatement> statements = new ArrayList<>();
        
        try {
            // parse statement 
            BufferedReader br = new BufferedReader(new FileReader(testFilename));
            String line = br.readLine();

            Statement stmt = parser.parse(line);
            
            ProofGenerator generator = new ProofGenerator(stmt);
            ArrayList<Statement> stmts = generator.generate();

            Proof proof = new Proof(new ArrayList<>(), Helper.dummyAnnotate(stmts));
            ArrayList<ProofAnnotator.AnnotatedStatement> annotatedStmts = proof.optimize().annotate().getStatements();

            for (int i = 0; i < annotatedStmts.size(); i++) {
                ProofAnnotator.AnnotatedStatement annStmt = annotatedStmts.get(i);
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
            System.out.println("Ошибка при парсинге " + e.getLine());
        } catch (AnnotatorException e) {
            System.err.println("Ошибка при аннотации " + e.getStatement() + ": " + e.getMessage());
        } catch (GeneratorException e) {
            System.out.print("Высказывание ложно при ");
            for (Entry<String, Boolean> entry : e.getValues().entrySet()) {
                System.out.print(" " + entry.getKey() + "=" + (entry.getValue() ? "1" : "0"));
            }
            System.out.println();
        }
    }
}
