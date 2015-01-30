public class Disjunction extends BinaryOperation {
    private static final boolean[] truthTable = new boolean[]{ false, true, true, true };
    
    public Disjunction(Statement left, Statement right) {
        super("|", left, right);
    }

    protected boolean estimateImpl(boolean a, boolean b) {
        return a | b;
    }

    protected boolean[] getTruthTable() {
        return truthTable;
    }
}
