public class Conjunction extends BinaryOperation {
    private static final boolean[] truthTable = new boolean[]{ false, false, false, true };

    public Conjunction(Statement left, Statement right) {
        super("&", left, right);
    }

    protected boolean estimateImpl(boolean a, boolean b) {
        return a & b;
    }

    protected boolean[] getTruthTable() {
        return truthTable;
    }
}
