import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Homework5 {
    public static class OpenSet {
        public List<Interval> bounds;

        public OpenSet() {
            bounds = new LinkedList<>();
        }

        public OpenSet(List<Interval> bounds) {
            this.bounds = bounds;
        }

        public OpenSet(Interval... bounds) {
            this.bounds = Arrays.asList(bounds);
        }

        public static OpenSet R() {
            OpenSet r = new OpenSet();
            r.bounds.add(new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
            return r;
        }

        public String toString() {
            if (bounds.size() == 0) {
                return "()";
            } else {
                StringBuilder out = new StringBuilder();
                for (Interval p : bounds) {
                    out.append(p);
                }
                return out.toString().replaceAll("Infinity", "inf");
            }
        }

        public OpenSet cutSinglePoint() {
            List<Interval> newBounds = new LinkedList<>(bounds);
            double upperBound = getUpperBound();
            double lowerBound = getLowerBound();
            double newPoint = (upperBound + lowerBound) / 2;
            if (Double.isNaN(newPoint)) {
                newPoint = 0.0;
            } else if (newPoint == Double.NEGATIVE_INFINITY) {
                newPoint = getUpperBound() - 1000.0;
            } else if (newPoint == Double.POSITIVE_INFINITY) {
                newPoint = getLowerBound() + 1000.0;
            }
            newBounds.remove(0);
            newBounds.add(0, new Interval(newPoint, upperBound));
            newBounds.add(0, new Interval(lowerBound, newPoint));
            return new OpenSet(newBounds);
        }

        public List<OpenSet> cutInParts(int n) {
            if (n == 0) {
                return new LinkedList<>();
            } else if (n == 1) {
                List<OpenSet> sets = new LinkedList<>();
                sets.add(this);
                return sets;
            }

            OpenSet newSet = cutSinglePoint();
            List<OpenSet> sets = new LinkedList<>();
            
            if (getUpperBound() == Double.POSITIVE_INFINITY && getLowerBound() != Double.NEGATIVE_INFINITY) {
                sets.addAll(new OpenSet(new Interval(newSet.getLowerBound(), newSet.getUpperBound())).cutInParts(n - 1));
                sets.add(new OpenSet(new Interval(newSet.getUpperBound(), Double.POSITIVE_INFINITY)));
            } else {
                sets.add(new OpenSet(new Interval(newSet.getLowerBound(), newSet.getUpperBound())));
                newSet.bounds.remove(0);
                sets.addAll(newSet.cutInParts(n - 1));
            }
            
            return sets;
        }

        public OpenSet union(OpenSet other) {
            List<Interval> newBounds = new LinkedList<>(bounds);
            List<Interval> res = new LinkedList<>();
            newBounds.addAll(other.bounds);
            Collections.sort(newBounds);

            while (newBounds.size() > 1) {
                Interval p1 = newBounds.get(0);
                Interval p2 = newBounds.get(1);
                newBounds.remove(0);
                newBounds.remove(0);
                // Some macaroni
                if (p2.l == p1.l && p2.r == p1.l) {
                    int fl = Interval.LEFT;
                    int fr = p1.f & Interval.RIGHT;
                    newBounds.add(0, new Interval(p1.l, p1.r, fl | fr));
                } else if (p2.l == p1.l && p1.l < p2.r && p2.r < p1.r) {
                    int fl = (p1.f & Interval.LEFT) | (p2.f & Interval.LEFT);
                    int fr = p1.f & Interval.RIGHT;
                    newBounds.add(0, new Interval(p1.l, p1.r, fl | fr));
                } else if (p2.l == p1.l && p2.r == p1.r) {
                    int fl = (p1.f & Interval.LEFT) | (p2.f & Interval.LEFT);
                    int fr = (p1.f & Interval.RIGHT) | (p2.f & Interval.RIGHT);
                    newBounds.add(0, new Interval(p1.l, p1.r, fl | fr));
                } else if (p2.l == p1.l && p2.r > p1.r) {
                    int fl = (p1.f & Interval.LEFT) | (p2.f & Interval.LEFT);
                    int fr = p2.f & Interval.RIGHT;
                    newBounds.add(0, new Interval(p1.l, p2.r, fl | fr));
                } else if (p1.l < p2.l && p2.l < p1.r && p1.l < p2.r && p2.r < p1.r) {
                    newBounds.add(0, p1);
                } else if (p1.l < p2.l && p2.l < p1.r && p2.r == p1.r) {
                    int fl = p1.f & Interval.LEFT;
                    int fr = (p1.f & Interval.RIGHT) | (p2.f & Interval.RIGHT);
                    newBounds.add(0, new Interval(p1.l, p1.r, fl | fr));
                } else if (p1.l < p2.l && p2.l < p1.r && p2.r > p1.r) {
                    int fl = p1.f & Interval.LEFT;
                    int fr = p2.f & Interval.RIGHT;
                    newBounds.add(0, new Interval(p1.l, p2.r, fl | fr));
                } else if (p2.l == p1.r && p2.r == p1.r) {
                    int fl = p1.f & Interval.LEFT;
                    int fr = Interval.RIGHT;
                    newBounds.add(0, new Interval(p1.l, p1.r, fl | fr));
                } else if (p2.l == p1.r && p2.r > p1.r && (p1.right() || p2.left())) {
                    int fl = p1.f & Interval.LEFT;
                    int fr = p2.f & Interval.RIGHT;
                    newBounds.add(0, new Interval(p1.l, p2.r, fl | fr));
                } else {
                    res.add(p1);
                    newBounds.add(0, p2);
                }
            }
            res.addAll(newBounds);
            Collections.sort(res);

            return new OpenSet(res);
        }

        public OpenSet intersect(OpenSet other) {
            List<Interval> res = new LinkedList<>();
            for (Interval p1 : bounds) {
                for (Interval p2 : other.bounds) {
                    double ll = Math.max(p1.l, p2.l);
                    double rr = Math.min(p1.r, p2.r);
                    int fl, fr;

                    if (p1.l < p2.l) {
                        fl = p2.f & Interval.LEFT;
                    } else if (p2.l < p1.l) {
                        fl = p1.f & Interval.LEFT;
                    } else {
                        fl = (p1.f & Interval.LEFT) & (p2.f & Interval.LEFT);
                    }

                    if (p1.r < p2.r) {
                        fr = p1.f & Interval.RIGHT;
                    } else if (p2.r < p1.r) {
                        fr = p2.f & Interval.RIGHT;
                    } else {
                        fr = (p1.f & Interval.RIGHT) & (p2.f & Interval.RIGHT);
                    }

                    if ((fl != 0 && fr != 0 && ll <= rr) || (ll < rr)) {
                        res.add(new Interval(ll, rr, fl | fr));
                    }
                }
            }
            return new OpenSet(res);
        }

        public OpenSet negate() {
            if (bounds.size() == 0) {
                return OpenSet.R();
            } else {
                OpenSet theRest = new OpenSet(new LinkedList<>(bounds.subList(1, bounds.size())));
                int fl = bounds.get(0).left() ? 0 : Interval.LEFT;
                int fr = bounds.get(0).right() ? 0 : Interval.RIGHT;
                OpenSet newSet = new OpenSet();
                if (getLowerBound() != Double.NEGATIVE_INFINITY) {
                    newSet.bounds.add(new Interval(Double.NEGATIVE_INFINITY, getLowerBound(), fr));
                }
                if (getUpperBound() != Double.POSITIVE_INFINITY) {
                    newSet.bounds.add(new Interval(getUpperBound(), Double.POSITIVE_INFINITY, fl));
                }
                OpenSet res = newSet.intersect(theRest.negate());
                return res;
            }
        }

        public OpenSet interior() {
            List<Interval> newBounds = new LinkedList<>();
            for (Interval p : bounds) {
                if (p.l < p.r) {
                    newBounds.add(p);
                }
            }
            return new OpenSet(newBounds);
        }

        private double getLowerBound() {
            return bounds.get(0).l;
        }

        private double getUpperBound() {
            return bounds.get(0).r;
        }

        private static class Interval implements Comparable<Interval> {
            double l;
            double r;

            int f;

            static int LEFT = 0x1;
            static int RIGHT = 0x2;

            private static double eps = 1e-6;

            public Interval(double l, double r) {
                this.l = l;
                this.r = r;
                this.f = 0;
            }

            public Interval(double l, double r, int f) {
                this.l = l;
                this.r = r;
                this.f = f;
            }

            @Override
            public int compareTo(Interval other) {
                double l1 = left() ? l : l + eps;
                double r1 = right() ? r : r - eps;
                double l2 = other.left() ? other.l : other.l + eps;
                double r2 = other.right() ? other.r : other.r - eps;

                if (l1 < l2) {
                    return -1;
                } else if (l1 == l2 && r1 < r2) {
                    return -1;
                } else {
                    return 1;
                }
            }

            public String toString() {
                String lend = left() ? "[" : "(";
                String rend = right() ? "]" : ")";
                return String.format("%s%f;%f%s", lend, l, r, rend);
            }

            public boolean left() {
                return (f & LEFT) != 0;
            }

            public boolean right() {
                return (f & RIGHT) != 0;
            }
        }
    }

    public static class Tree {
        public Set<String> forced;
        public Set<String> unwanted;
        public List<Tree> children;
        OpenSet set;

        public Tree() {
            this.forced = new HashSet<>();
            this.unwanted = new HashSet<>();
            this.children = new LinkedList<>();
        }

        public Tree(Set<String> forced, Set<String> unwanted, List<Tree> children) {
            this.forced = forced;
            this.unwanted = unwanted;
            this.children = children;
        }

        public String toString() {
            StringBuilder out = new StringBuilder();
           
            out.append("(");
            for (String force : forced) {
                out.append(force).append(",");
            }
            if (forced.size() != 0) {
                out.deleteCharAt(out.length() - 1);
            }
            out.append(")\n");

            for (Tree child : children) {
                String[] childLines = child.toString().split("\\n");
                for (String line : childLines) {
                    out.append("  ").append(line).append("\n");
                }
            }

            return out.toString();
        }

        public void evaluateTree(OpenSet startSet, HashMap<String, OpenSet> varSets) {
            set = startSet;

            for (String force : forced) {
                OpenSet updSet = startSet;
                if (varSets.containsKey(force)) {
                    updSet = varSets.get(force).union(startSet);
                }
                varSets.put(force, updSet);
            }
            
            if (children.size() == 1) {
                OpenSet childSet = startSet.cutSinglePoint();
                children.get(0).evaluateTree(childSet, varSets);
            } else {
                int n = children.size();
                List<OpenSet> childSets = startSet.cutInParts(n);
                for (int i = 0; i < n; i++) {
                    children.get(i).evaluateTree(childSets.get(i), varSets);
                }
            }
        }

        public boolean isProvable(Statement stmt) {
            return forall(w -> w.isForced(stmt));
        }

        public boolean isDisprovable(Statement stmt) {
            return exists(w -> w.isUnforced(stmt));
        }

        public boolean isForced(Statement stmt) {
            if (stmt instanceof Predicate) {
                String name = ((Predicate) stmt).name;
                return forced.contains(name);
            }
            
            else if (stmt instanceof Negation) {
                Statement notStmt = ((Negation) stmt).child;
                return forall(w -> !w.isForced(notStmt));
            }
            
            else if (stmt instanceof Conjunction) {
                Statement left = ((Conjunction) stmt).left;
                Statement right = ((Conjunction) stmt).right;
                return isForced(left) && isForced(right);
            }
            
            else if (stmt instanceof Disjunction) {
                Statement left = ((Disjunction) stmt).left;
                Statement right = ((Disjunction) stmt).right;
                return isForced(left) || isForced(right);
            }
            
            else if (stmt instanceof Implication) {
                Statement left = ((Implication) stmt).left;
                Statement right = ((Implication) stmt).right;
                return forall(w -> !w.isForced(left) || w.isForced(right));
            }

            else {
                throw new IllegalArgumentException();
            }
        }

        public boolean isUnforced(Statement stmt) {
            if (stmt instanceof Predicate) {
                String name = ((Predicate) stmt).name;
                return !forced.contains(name);
            }
            
            else if (stmt instanceof Negation) {
                Statement notStmt = ((Negation) stmt).child;
                return exists(w -> w.isForced(notStmt));
            }
            
            else if (stmt instanceof Conjunction) {
                Statement left = ((Conjunction) stmt).left;
                Statement right = ((Conjunction) stmt).right;
                return isUnforced(left) || isUnforced(right);
            }
            
            else if (stmt instanceof Disjunction) {
                Statement left = ((Disjunction) stmt).left;
                Statement right = ((Disjunction) stmt).right;
                return isUnforced(left) && isUnforced(right);
            }
            
            else if (stmt instanceof Implication) {
                Statement left = ((Implication) stmt).left;
                Statement right = ((Implication) stmt).right;
                return exists(w -> w.isForced(left) && w.isUnforced(right));
            }

            else {
                throw new IllegalArgumentException();
            }
        }

        public static Tree disprove(Statement stmt) {
            List<Tree> forest = Tree.createWithUnforced(stmt);
            for (Tree tree : forest) {
                if (tree.isDisprovable(stmt)) {
                    return tree;
                }
            }
            return null;
        }

        public static List<Tree> createWithForced(Statement force) {
            if (force instanceof Predicate) {
                String name = ((Predicate) force).name;
                return singleL(new Tree(singleS(name), emptyS(), emptyL()));
            }
            
            else if (force instanceof Negation) {
                Statement unforce = ((Negation) force).child;
                return hangDown(createWithUnforced(unforce));
            }
            
            else if (force instanceof Conjunction) {
                Statement left = ((Conjunction) force).left;
                Statement right = ((Conjunction) force).right;
                return mergeTrees(createWithForced(left), createWithForced(right));
            }
            
            else if (force instanceof Disjunction) {
                Statement left = ((Disjunction) force).left;
                Statement right = ((Disjunction) force).right;
                return concat(createWithForced(left), createWithForced(right));
            
            } else if (force instanceof Implication) {
                Statement left = ((Implication) force).left;
                Statement right = ((Implication) force).right;
                return hangDown(concat(concat(
                    mergeTrees(createWithForced(left), createWithForced(right)),
                    mergeTrees(createWithUnforced(left), createWithForced(right))),
                    mergeTrees(createWithUnforced(left), createWithUnforced(right))));
            }
            
            else {
                throw new IllegalArgumentException();
            }
        }

        public static List<Tree> createWithUnforced(Statement unforce) {
            if (unforce instanceof Predicate) {
                String name = ((Predicate) unforce).name;
                return singleL(new Tree(emptyS(), singleS(name), emptyL()));
            }
            
            else if (unforce instanceof Negation) {
                Statement force = ((Negation) unforce).child;
                return hangDown(createWithForced(force));
            }
            
            else if (unforce instanceof Conjunction) {
                Statement left = ((Conjunction) unforce).left;
                Statement right = ((Conjunction) unforce).right;
                return concat(createWithUnforced(left), createWithUnforced(right));
            }
            
            else if (unforce instanceof Disjunction) {
                Statement left = ((Disjunction) unforce).left;
                Statement right = ((Disjunction) unforce).right;
                return mergeTrees(createWithUnforced(left), createWithUnforced(right));
            
            } else if (unforce instanceof Implication) {
                Statement left = ((Implication) unforce).left;
                Statement right = ((Implication) unforce).right;
                return hangDown(mergeTrees(createWithForced(left), createWithUnforced(right)));
            }
            
            else {
                throw new IllegalArgumentException();
            }
        }

        private <T> T walk(BiFunction<T, Tree, T> walker, T start) {
            T value = walker.apply(start, this);

            for (Tree child : children) {
                value = child.walk(walker, value);
            }

            return value;
        }

        private boolean forall(Function<Tree, Boolean> p) {
            return walk((r, t) -> r && p.apply(t), true);
        }

        private boolean exists(Function<Tree, Boolean> p) {
            return walk((r, t) -> r || p.apply(t), false);
        }

        private static List<Tree> hangDown(List<Tree> branches) {
            List<Tree> result = emptyL();
            for (Tree child : branches) {
                result.add(new Tree(emptyS(), emptyS(), singleL(child)));
            }
            return result;
        }

        private static Set<String> collectUnwanted(Tree tree) {
            Set<String> all = new HashSet<>(tree.unwanted);
            for (Tree child : tree.children) {
                all.addAll(collectUnwanted(child));
            }
            return all;
        }

        private static Tree mergeTwo(Tree fst, Tree snd) {
            Set<String> unwantedF = collectUnwanted(fst);
            Set<String> unwantedS = collectUnwanted(snd);

            if (!intersect(fst.forced, unwantedS).isEmpty()) {
                return null;
            } else if (!intersect(unwantedF, snd.forced).isEmpty()) {
                return null;
            } else {
                return new Tree(
                    union(fst.forced, snd.forced),
                    union(unwantedF, unwantedS),
                    concat(fst.children, snd.children));
            }
        }

        private static List<Tree> mergeTrees(List<Tree> fsts, List<Tree> snds) {
            List<Tree> all = emptyL();

            for (Tree fst : fsts) {
                for (Tree snd : snds) {
                    Tree tree = mergeTwo(fst, snd);
                    if (tree != null) {
                        all.add(tree);
                    }
                }
            }

            return all;
        }

        private static <T> List<T> emptyL() {
            return new LinkedList<T>();
        }

        private static <T> Set<T> emptyS() {
            return new HashSet<T>();
        }

        private static <T> List<T> singleL(T elem) {
            return Arrays.asList(elem);
        }

        private static <T> Set<T> singleS(T elem) {
            return new HashSet<T>(Arrays.asList(elem));
        }

        private static <T> Set<T> union(Set<T> fst, Set<T> snd) {
            Set<T> res = new HashSet<T>(fst);
            res.addAll(snd);
            return res;
        }

        private static <T> List<T> concat(List<T> fst, List<T> snd) {
            List<T> res = new LinkedList<T>(fst);
            res.addAll(snd);
            return res;
        }

        private static <T> Set<T> intersect(Set<T> fst, Set<T> snd) {
            Set<T> res = new HashSet<T>(fst);
            res.retainAll(snd);
            return res;
        }
    }
        
    public static OpenSet evaluateStatement(Statement stmt, HashMap<String, OpenSet> varSets) {
        if (stmt instanceof Predicate) {
            String name = ((Predicate) stmt).name;
            if (varSets.containsKey(name)) {
                return varSets.get(name);
            } else {
                return new OpenSet();
            }
        }
        
        else if (stmt instanceof Negation) {
            Statement notStmt = ((Negation) stmt).child;
            return evaluateStatement(notStmt, varSets).negate().interior();
        }
        
        else if (stmt instanceof Conjunction) {
            Statement left = ((Conjunction) stmt).left;
            Statement right = ((Conjunction) stmt).right;
            return evaluateStatement(left, varSets).intersect(evaluateStatement(right, varSets));
        }
        
        else if (stmt instanceof Disjunction) {
            Statement left = ((Disjunction) stmt).left;
            Statement right = ((Disjunction) stmt).right;
            return evaluateStatement(left, varSets).union(evaluateStatement(right, varSets));
        }
        
        else if (stmt instanceof Implication) {
            Statement left = ((Implication) stmt).left;
            Statement right = ((Implication) stmt).right;
            return evaluateStatement(left, varSets).negate().union(evaluateStatement(right, varSets)).interior();
        }

        else {
            throw new IllegalArgumentException();
        }
    }

    public static void main(String[] args) {
        String testFilename = args[0];

        PredicateParser parser = new PredicateParser();

        try {
            BufferedReader br = new BufferedReader(new FileReader(testFilename));
            String line = br.readLine();
            Statement toDisprove = parser.parse(line);

            Tree kTree = Tree.disprove(toDisprove);

            if (kTree != null) {
                HashMap<String, OpenSet> varSets = new HashMap<>();
                kTree.evaluateTree(OpenSet.R(), varSets);

                OpenSet ans = evaluateStatement(toDisprove, varSets);
                for (Map.Entry<String, OpenSet> entry : varSets.entrySet()) {
                    System.out.println(entry.getKey() + " = " + entry.getValue());
                }
                System.out.println(ans);
            } else {
                System.out.println("Формула общезначима");
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        } catch (ParserException e) {
            System.err.println("Ошибка при парсинге " + e.getLine());
        }
    }
}
