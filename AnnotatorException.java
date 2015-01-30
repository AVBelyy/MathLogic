public class AnnotatorException extends Exception {
    private final Statement statement;
    private final String message;

    public AnnotatorException(Statement statement, String message) {
        this.statement = statement;
        this.message = message;
    }

    public Statement getStatement() {
        return statement;
    }

    public String getMessage() {
        return message;
    }
}
