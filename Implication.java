public class Implication extends BinaryOperation {
    private static final boolean[] truthTable = new boolean[]{ true, true, false, true };

    public Implication(Statement left, Statement right) {
        super("->", left, right);
    }

    protected boolean estimateImpl(boolean a, boolean b) {
        return !a | b;
    }

    protected boolean[] getTruthTable() {
        return truthTable;
    }
}
