package tv.xrm.jfilter;


import java.text.Collator;
import java.util.*;

/**
 * Textual specifications for sub-trees, vaguely similar to XPath. Basically, each path down the tree is added to the
 * spec as a string. In case of overlap, more general spec wins, i.e. "foo.bar" will overwrite "foo.bar.baz".
 */
public class Spec {

    public static final String WILDCARD = "*";

    // NB: if SEPARATOR changes, regexes in code need to change as well
    public static final String SEPARATOR = ".";

    private final Collection<String> specs = new TreeSet<>(Collator.getInstance());

    public Spec() {
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

    public static Spec spec(final String... specs) {
        return new Spec().add(specs);
    }

    synchronized Collection<String> normalised() {
        List<String> normal = new ArrayList<>(specs.size());

        String prev = "";
        for (String s : specs) {
            if (prev.isEmpty() || !s.startsWith(prev + SEPARATOR)) {
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

        return convert("ROOT", split).getChildren();
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
        List<String> result = new ArrayList<>(Arrays.asList(cleanSpec.split("\\.")));
        for (int i = 0; i < result.size(); i++) {
            String token = result.get(i);
            if (token.equals(WILDCARD)) {
                result.set(i, FilteredTreeCopier.WILDCARD);
            }
        }
        return result;
    }

    static String cleanup(final String spec) {
        String c = spec.replaceAll("(\\.)\\1+", "$1");
        c = c.replaceAll("^(\\.)+", "");
        c = c.replaceAll("(\\.)$", "");
        return c;
    }

}
