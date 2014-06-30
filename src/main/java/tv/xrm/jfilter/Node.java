package tv.xrm.jfilter;

import java.util.ArrayList;
import java.util.Arrays;
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
        this(name, new ArrayList<Node>());
    }

    String getName() {
        return name;
    }

    List<Node> getChildren() {
        return children;
    }

    public synchronized Node deepCopy() {
        return deepCopy(this);
    }

    private synchronized Node deepCopy(Node node) {
        List<Node> children = node.children;
        if (children != null) {
            List<Node> childCopies = new ArrayList<>(children.size());
            for (Node child : children) {
                childCopies.add(deepCopy(child));
            }
            return new Node(node.getName(), childCopies);
        }
        return new Node(node.getName());
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
