import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.BiFunction;

public class Homework5 {
    static class Tree {
        public Set<String> forced;
        public Set<String> unwanted;
        public List<Tree> children;

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

    public static void main(String[] args) {
        System.out.println(Tree.disprove(Helper.parseKnownLine("(!B->!A)->(A->B)")));
    }
}
