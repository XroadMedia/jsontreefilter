package tv.xrm.jfilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Sub-tree specification node for FilteredTreeCopier. This is the "programmatic" specification.
 *
 * @see tv.xrm.jfilter.FilteredTreeCopier
 * @see tv.xrm.jfilter.Spec
 */
public final class Node {
    private final String name;
    private final List<Node> children;

    public Node(final String name, final List<Node> children) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.children = Objects.requireNonNull(children, "children must not be null");
    }

    public Node(final String name, Node... children) {
        this(name, Arrays.asList(children));
    }

    public Node(final String name) {
        this(name, Collections.<Node>emptyList());
    }

    String getName() {
        return name;
    }

    List<Node> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("'").append(name).append("'(");

        boolean first = true;
        for (Node child : children) {
            if (first) {
                first = false;
            } else {
                b.append(' ');
            }
            b.append(child);
        }

        b.append(')');
        return b.toString();
    }

}
