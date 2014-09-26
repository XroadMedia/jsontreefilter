package tv.xrm.jfilter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Textual specifications for sub-trees, vaguely similar to XPath. Basically, each path down the tree is added to the
 * spec as a string. In case of overlap, more general spec wins, i.e. "foo.bar" will overwrite "foo.bar.baz".
 */
public class Spec {

    public static final String WILDCARD = "*";

    // NB: if SEPARATOR changes, regexes in code need to change as well
    public static final String SEPARATOR = ".";

    public static final String BRACKET_FOR_ESCAPING = "'";

    private final Collection<String> specs = new TreeSet<>(Collator.getInstance());

    private final OverlappingBehaviour behaviour;

    public Spec() {
        behaviour = OverlappingBehaviour.UNION;
    }

    public Spec(OverlappingBehaviour behaviour) {
        this.behaviour = behaviour;
        if (behaviour == null) {
            behaviour = OverlappingBehaviour.UNION;
        }
    }

    public synchronized Spec add(final String spec) {
        if (spec == null) {
            throw new IllegalArgumentException("spec must not be null");
        }

        String cleaned = cleanup(spec);
        if (!cleaned.isEmpty()) {
            this.specs.add(cleaned);
        }

        return this;
    }

    public synchronized Spec add(final String... specs) {
        for (String s : specs) {
            add(s);
        }
        return this;
    }
    
    public synchronized Spec add(final Iterable<String> specs) {
        for (String s : specs) {
            add(s);
        }
        return this;
    }

    public static Spec spec(final String... specs) {
        return new Spec().add(specs);
    }

    public static Spec spec(final OverlappingBehaviour behaviour, final String... specs) {
        return new Spec(behaviour).add(specs);
    }
    
    public static Spec spec(final OverlappingBehaviour behaviour, final Iterable<String> specs) {
        return new Spec(behaviour).add(specs);
    }

    synchronized Collection<String> normalised() {
        List<String> normal = new ArrayList<>(specs.size());

        String prev = "";
        for (String s : specs) {
            if (behaviour.equals(OverlappingBehaviour.INTERSECTION) || prev.isEmpty()
                    || !s.startsWith(prev + SEPARATOR)) {
                normal.add(s);
            }
            prev = s;
        }

        return normal;
    }

    public synchronized List<Node> toNodes() {
        final List<List<String>> split = new ArrayList<>();
        for (String spec : normalised()) {
            split.add(split(spec));
        }

        List<Node> children = convert("ROOT", split).getChildren();
        if (behaviour.equals(OverlappingBehaviour.INTERSECTION)) {
            intersectChildNodes(children);
        }
        return children;
    }

    private void intersectChildNodes(List<Node> children) {
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                Node child = children.get(i);
                if (child.getName().equals(FilteredTreeCopier.WILDCARD)) {
                    // copy children of child to neighbours
                    if (child.getChildren() != null && !child.getChildren().isEmpty() && children.size() > 1) {
                        for (int j = 0; j < children.size(); j++) {
                            if (j == i) {
                                continue;
                            }
                            children.get(j).getChildren().addAll(copyChildren(child.getChildren()));
                        }
                        children.remove(i--);
                        continue;
                    }
                }
                if (child.getChildren() != null) {
                    intersectChildNodes(child.getChildren());
                }
            }
        }
    }

    private List<Node> copyChildren(List<Node> nodes) {
        List<Node> result = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            result.add(n.deepCopy());
        }
        return result;
    }

    Node convert(String thisName, final Collection<List<String>> specs) {
        final Map<String, List<List<String>>> subGroups = extractSubgroups(specs);

        List<Node> subNodes = new ArrayList<>();
        for (Map.Entry<String, List<List<String>>> entry : subGroups.entrySet()) {
            subNodes.add(convert(entry.getKey(), entry.getValue()));
        }

        return new Node(thisName, subNodes);
    }

    private Map<String, List<List<String>>> extractSubgroups(Collection<List<String>> specs) {
        final Map<String, List<List<String>>> subGroups = new HashMap<>();
        for (List<String> s : specs) {
            if (!s.isEmpty()) {
                String name = s.get(0);
                List<String> rest = s.subList(1, s.size());
                List<List<String>> existing = subGroups.get(name);
                if (existing == null) {
                    existing = new ArrayList<>();
                }
                existing.add(rest);
                subGroups.put(name, existing);
            }
        }
        return subGroups;
    }

    static List<String> split(final String cleanSpec) {
        // we're assuming here the spec has gone through cleanup first
        List<String> result = new ArrayList<>();
        List<String> bracketSplitList = Arrays.asList(cleanSpec.split(BRACKET_FOR_ESCAPING));
        for (int i = 0; i < bracketSplitList.size(); i++) {
            String bracketToken = bracketSplitList.get(i);
            if (bracketToken.isEmpty()) {
                continue;
            }
            List<String> dotSplitList;
            if (i % 2 == 1) {
                dotSplitList = Arrays.asList(bracketToken);
            } else {
                dotSplitList = new ArrayList<>(Arrays.asList(bracketToken.split("\\.")));
            }
            for (String token : dotSplitList) {
                if (token.equals(WILDCARD)) {
                    result.add(FilteredTreeCopier.WILDCARD);
                } else if (!token.isEmpty()) {
                    result.add(token);
                }
            }
        }
        return result;
    }

    static String cleanup(final String spec) {
        String[] arr = spec.split(BRACKET_FOR_ESCAPING);
        if (arr.length % 2 == 0) {
            if (spec.charAt(spec.length() - 1) != '\'') {
                throw new IllegalArgumentException("invalid bracket notation in spec");
            }
        }
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                cleaned.append(".");
            }
            if (i % 2 == 0) {
                String c = arr[i].replaceAll("(\\.)\\1+", "$1");
                c = c.replaceAll("^(\\.)+", "");
                cleaned.append(c.replaceAll("(\\.)$", ""));
            } else {
                cleaned.append(BRACKET_FOR_ESCAPING).append(arr[i]).append(BRACKET_FOR_ESCAPING);
            }
        }
        return cleaned.toString();
    }

    public static enum OverlappingBehaviour {
        INTERSECTION, UNION
    }
}
