import java.util.HashMap;

public class GeneratorException extends Exception {
    private final HashMap<String, Boolean> values;

    public GeneratorException(HashMap<String, Boolean> values) {
        this.values = values;
    }

    public HashMap<String, Boolean> getValues() {
        return values;
    }
}
