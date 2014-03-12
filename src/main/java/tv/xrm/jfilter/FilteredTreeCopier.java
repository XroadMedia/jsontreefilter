package tv.xrm.jfilter;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Given a Jackson Tree Model, copies a sub-tree from it based on a simple specification of object field names.
 * The sub-tree always starts at the root, so it's basically a pruned copy. Trivial wildcards are supported.
 */
public final class FilteredTreeCopier {

    public static final String WILDCARD = "";

    private FilteredTreeCopier() {
    }

    /**
     * Copy a sub-tree from the given tree, based on a hierarchical specification: a tree of field names.
     * <p/>
     * The returned tree is separate from the original one, i.e. it can be safely modified. Nodes are generally
     * copied (unless they are immutable).
     */
    public static JsonNode copyTree(final JsonNode root, final List<Node> nameNodes) {
        return copyOrShadow(root, nameNodes, true);
    }

    /**
     * Shadow a sub-tree from the given tree, based on a hierarchical specification: a tree of field names.
     * <p/>
     * The returned tree may share container objects with the original one, i.e. changes on either tree
     * may be seen in the other. Depending on the JSON and the filter spec, this can result in dramatically
     * less memory and CPU consumption than copyTree(). However, it's potentially unsafe, particularly under
     * concurrency.
     *
     * @see tv.xrm.jfilter.FilteredTreeCopier#copyTree(com.fasterxml.jackson.databind.JsonNode, java.util.List)
     */
    public static JsonNode shadowTree(final JsonNode root, final List<Node> nameNodes) {
        return copyOrShadow(root, nameNodes, false);
    }

    private static JsonNode copyOrShadow(final JsonNode root, final List<Node> nameNodes, final boolean copy) {
        if (root.isObject()) {
            final ObjectNode object = (ObjectNode) root;

            if (!nameNodes.isEmpty()) {
                final ObjectNode newObject = object.objectNode();

                for (final Node node : nameNodes) {
                    final String name = node.getName();

                    if (isNotWildcard(name)) {
                        // specific name
                        final JsonNode child = object.get(name);
                        if (child != null) {
                            newObject.put(name, copyOrShadow(child, node.getChildren(), copy));
                        }
                    } else {
                        // "all children"
                        final Iterator<Map.Entry<String, JsonNode>> children = object.fields();
                        while (children.hasNext()) {
                            final Map.Entry<String, JsonNode> child = children.next();
                            newObject.put(child.getKey(), copyOrShadow(child.getValue(), node.getChildren(), copy));
                        }
                    }
                }

                return newObject;
            } else {
                // no further names, but further objects down this branch - copy/shadow fully
                return deepCopyOrRef(root, copy);
            }
        } else {
            // a non-object is always copied/shadowed fully (names do not apply - our processing stops here)
            return deepCopyOrRef(root, copy);
        }
    }

    private static boolean isNotWildcard(final Object name) {
        return !name.equals(WILDCARD);
    }

    private static JsonNode deepCopyOrRef(final JsonNode root, final boolean copy) {
        return copy ? root.deepCopy() : root;
    }

}
