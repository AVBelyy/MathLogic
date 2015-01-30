public class ParserException extends Exception {
    private final String line;

    public ParserException(String line) {
        this.line = line;
    }

    public String getLine() {
        return line;
    }
}
