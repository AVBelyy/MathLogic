import java.util.Stack;
import java.util.EmptyStackException;

// Failsafe parser left in project for no reason at all

public class PropositionalParser {
    private static boolean isDelimeter(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private static boolean isOperator(char c) {
        return c == '!' || c == '&' || c == '|' || c == '>';
    }

    private static int getPriority(char op) {
        if (op == '!') {
            return 4;
        } else if (op == '&') {
            return 3;
        } else if (op == '|') {
            return 2;
        } else if (op == '>') {
            return 1;
        } else {
            return -1;
        }
    }

    private static boolean isLeftAssoc(char op) {
        if (op == '>' || op == '!') {
            return false;
        } else {
            return true;
        }
    }

    Stack<Statement> st;
    Stack<Character> op;

    public PropositionalParser() {
        this.st = new Stack<Statement>();
        this.op = new Stack<Character>();
    }

    private void parseOp(char op) {
        if (op == '!') {
            Statement l = st.pop();
            Negation neg = new Negation(l);
            st.push(neg);
        } else {
            Statement stmt = null;
            Statement r = st.pop();
            Statement l = st.pop();
            if (op == '&') stmt = new Conjunction(l, r);
            if (op == '|') stmt = new Disjunction(l, r);
            if (op == '>') stmt = new Implication(l, r);
            st.push(stmt);
        }
    }

    Statement parse(String s) throws ParserException {
        int sLength = s.length();
        for (int i = 0; i < sLength; i++) {
            if (!isDelimeter(s.charAt(i))) {
                if (s.charAt(i) == '-' && i + 1 < sLength && s.charAt(i + 1) == '>') {
                    i++; // "->" -> ">"
                }
                if (s.charAt(i) == '(') {
                    op.push('(');
                } else if (s.charAt(i) == ')') {
                    while (op.peek() != '(') {
                        parseOp(op.pop());
                    }
                    op.pop();
                } else if (isOperator(s.charAt(i))) {
                    char curOp = s.charAt(i);
                    while (!op.empty() && (
                            (isLeftAssoc(curOp) && getPriority(op.peek()) >= getPriority(curOp))
                        || (!isLeftAssoc(curOp) && getPriority(op.peek()) >  getPriority(curOp)))) {
                        parseOp(op.peek());
                        op.pop();
                    }
                    op.push(curOp);
                } else if ('A' <= s.charAt(i) && s.charAt(i) <= 'Z') {
                    String varName = "" + s.charAt(i);
                    i++;
                    while (i < sLength && '0' <= s.charAt(i) && s.charAt(i) <= '9') {
                        varName += s.charAt(i);
                        i++;
                    }
                    i--;
                    Variable var = new Variable(varName);
                    st.push(var);
                } else if ('0' <= s.charAt(i) && s.charAt(i) <= '9') {
                    Pattern pattern = new Pattern(s.charAt(i) - '0');
                    st.push(pattern);
                } else {
                    throw new ParserException(s);
                }
            }
        }

        while (!op.empty()) {
            parseOp(op.pop());
        }

        if (st.size() != 1) {
            throw new ParserException(s);
        }
        try {
            return st.pop();
        } catch (EmptyStackException e) {
            throw new ParserException(s);
        }
    }
}
