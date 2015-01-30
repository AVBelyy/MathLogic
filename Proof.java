import java.util.ArrayList;

// Helper class for convenience
public class Proof {
    private final ArrayList<Statement> context;
    private final ArrayList<ProofAnnotator.AnnotatedStatement> statements;

    private final ProofAnnotator annotator = new ProofAnnotator();
    private final ProofLiberator liberator = new ProofLiberator();

    public Proof() {
        this.context = new ArrayList<Statement>();
        this.statements = new ArrayList<ProofAnnotator.AnnotatedStatement>();
    }

    public Proof(ArrayList<Statement> context, ArrayList<ProofAnnotator.AnnotatedStatement> statements) {
        this.context = context;
        this.statements = statements;
    }

    public ArrayList<ProofAnnotator.AnnotatedStatement> getStatements() {
        return statements;
    }

    public Proof annotate() throws AnnotatorException {
        return new Proof(context, annotator.annotate(context, statements));
    }

    public Proof liberate() throws AnnotatorException {
        ArrayList<Statement> newContext = new ArrayList<Statement>(context);
        Statement hypothesis = newContext.remove(newContext.size() - 1);

        return new Proof(newContext, liberator.liberate(annotator.annotate(context, statements), hypothesis));
    }
    
    public Proof liberateAll() throws AnnotatorException {
        ArrayList<ProofAnnotator.AnnotatedStatement> newStatements = new ArrayList<>(statements);

        for (int i = context.size() - 1; i >= 0; i--) {
            Statement hypothesis = context.get(i);
            newStatements = liberator.liberate(annotator.annotate(context, newStatements), hypothesis);
        }

        return new Proof(new ArrayList<Statement>(/* empty context */), newStatements);
    }

    public Proof optimize() throws AnnotatorException {
        ArrayList<Statement> newStatements = Helper.optimizeAnnotated(annotator.annotate(context, statements));

        return new Proof(context, Helper.annotateInContext(context, newStatements));
    }
}
