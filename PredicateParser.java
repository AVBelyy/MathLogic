import java.util.ArrayList;

public class PredicateParser {
    String s;

    public PredicateParser() {
    }

    private Statement implication(int l, int r) throws ParserException {
        int balance = 0;

        for (int i = l; i < r; i++) {
            if (s.charAt(i) == '(') {
                balance++;
            } else if (s.charAt(i) == ')') {
                balance--;
            } else if (s.charAt(i) == '>' && balance == 0) {
                return new Implication(disjunction(l, i), implication(i + 1, r));
            }
        }

        return disjunction(l, r);
    }

    private Statement disjunction(int l, int r) throws ParserException {
        int balance = 0;
        int pos = -1;

        for (int i = l; i < r; i++) {
            if (s.charAt(i) == '(') {
                balance++;
            } else if (s.charAt(i) == ')') {
                balance--;
            } else if (s.charAt(i) == '|' && balance == 0) {
                pos = i;
            }
        }

        if (pos != -1) {
            return new Disjunction(disjunction(l, pos), conjunction(pos + 1, r));
        } else {
            return conjunction(l, r);
        }
    }

    private Statement conjunction(int l, int r) throws ParserException {
        int balance = 0;
        int pos = -1;

        for (int i = l; i < r; i++) {
            if (s.charAt(i) == '(') {
                balance++;
            } else if (s.charAt(i) == ')') {
                balance--;
            } else if (s.charAt(i) == '&' && balance == 0) {
                pos = i;
            }
        }

        if (pos != -1) {
            return new Conjunction(conjunction(l, pos), unary(pos + 1, r));
        } else {
            return unary(l, r);
        }
    }

    private Statement unary(int l, int r) throws ParserException {
        if (s.charAt(l) == '!') {
            return new Negation(unary(l + 1, r));
        }
        
        if (s.charAt(l) == '(' && s.charAt(r - 1) == ')') {
            int balance = 1;
            boolean flag = true;

            for (int i = l + 1; i < r - 1; i++) {
                if (s.charAt(i) == '(') {
                    balance++;
                } else if (s.charAt(i) == ')') {
                    balance--;
                }
                if (balance == 0) {
                    flag = false;
                    break;
                }
            }
            
            if (flag) {
                return implication(l + 1, r - 1);
            }
        }
        
        if (s.charAt(l) == '@') {
            String varName = variable(l + 1, r);
            return new Forall(varName, unary(l + 1 + varName.length(), r));
        }
        
        if (s.charAt(l) == '?') {
            String varName = variable(l + 1, r);
            return new Exists(varName, unary(l + 1 + varName.length(), r));
        }

        if (s.charAt(l) == '$') {
            int patternId = s.charAt(l + 1) - '1';
            return new Pattern(patternId);
        }
        
        return predicate(l, r);
    }

    private Statement predicate(int l, int r) throws ParserException {
        if ('A' <= s.charAt(l) && s.charAt(l) <= 'Z') {
            String predName = "" + s.charAt(l);
            int index = l + 1;
            while (index < r && '0' <= s.charAt(index) && s.charAt(index) <= '9') {
                predName += s.charAt(index++);
            }

            ArrayList<Expression> terms = new ArrayList<>();
            if (index < r && s.charAt(index) == '(' && s.charAt(r - 1) == ')') {
                index++;
                int prev = index;
                for (int i = index; i < r; i++) {
                    if (s.charAt(i) == ',' || i == r - 1) {
                        terms.add(term(prev, i));
                        prev = i + 1;
                    }
                }
            }
            
            return new Predicate(predName, terms);
        } else {
            for (int i = l; i < r; i++) {
                if (s.charAt(i) == '=') {
                    return new Equals(term(l, i), term(i + 1, r));
                }
            }
        }

        throw new ParserException(s);
    }

    private Expression term(int l, int r) throws ParserException {
        int balance = 0;
        int pos = -1;

        for (int i = l; i < r; i++) {
            if (s.charAt(i) == '(') {
                balance++;
            } else if (s.charAt(i) == ')') {
                balance--;
            } else if (s.charAt(i) == '+' && balance == 0) {
                pos = i;
            }
        }

        if (pos != -1) {
            return new Sum(term(l, pos), sum(pos + 1, r));
        } else {
            return sum(l, r);
        }
    }

    private Expression sum(int l, int r) throws ParserException {
        int balance = 0;
        int pos = -1;

        for (int i = l; i < r; i++) {
            if (s.charAt(i) == '(') {
                balance++;
            } else if (s.charAt(i) == ')') {
                balance--;
            } else if (s.charAt(i) == '*' && balance == 0) {
                pos = i;
            }
        }

        if (pos != -1) {
            return new Product(sum(l, pos), product(pos + 1, r));
        } else {
            return product(l, r);
        }
    }

    private Expression product(int l, int r) throws ParserException {
        if (s.charAt(r - 1) == '\'') {
            return new Succ(product(l, r - 1));
        }
        
        if (s.charAt(l) == '0') {
            return new Zero();
        }

        if (s.charAt(l) == '(' && s.charAt(r - 1) == ')') {
            return term(l + 1, r - 1);
        }

        if (s.charAt(l) == '#') {
            int patternId = s.charAt(l + 1) - '1';
            return new ArithmPattern(patternId);
        }

        String tokenName = variable(l, r);
        int index = l + tokenName.length();
        
        ArrayList<Expression> terms = new ArrayList<>();
        if (index < r && s.charAt(index) == '(' && s.charAt(r - 1) == ')') {
            index++;
            int prev = index;
            for (int i = index; i < r; i++) {
                if (s.charAt(i) == ',' || i == r - 1) {
                    terms.add(term(prev, i));
                    prev = i + 1;
                }
            }
        }
        return new Function(tokenName, terms);
    }

    private String variable(int l, int r) throws ParserException {
        if ('a' <= s.charAt(l) && s.charAt(l) <= 'z') {
            String varName = "" + s.charAt(l);
            int index = l + 1;
            while (index < r && '0' <= s.charAt(index) && s.charAt(index) <= '9') {
                varName += s.charAt(index++);
            }
            return varName;
        } else {
            throw new ParserException(s);
        }
    }

    public Statement parse(String s) throws ParserException {
        this.s = s.replaceAll("->", ">");
        return implication(0, this.s.length());
    }
}
