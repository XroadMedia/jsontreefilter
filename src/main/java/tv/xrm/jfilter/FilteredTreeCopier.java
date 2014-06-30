package tv.xrm.jfilter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Given a Jackson Tree Model, copies a sub-tree from it based on a simple specification of object field names. The
 * sub-tree always starts at the root, so it's basically a pruned copy. Trivial wildcards are supported.
 */
public final class FilteredTreeCopier {

    public static final String WILDCARD = "";

    private FilteredTreeCopier() {
    }

    /**
     * Copy a sub-tree from the given tree, based on a hierarchical specification: a tree of field names.
     * <p/>
     * The returned tree is separate from the original one, i.e. it can be safely modified. Nodes are generally copied
     * (unless they are immutable).
     */
    public static JsonNode copyTree(final JsonNode root, final List<Node> nameNodes) {
        return copyOrShadow(root, nameNodes, true, new LinkedList<String>(), null, null, true);
    }

    /**
     * Copy a sub-tree from the given tree, based on a hierarchical specification: a tree of field names.
     * <p/>
     * The returned tree is separate from the original one, i.e. it can be safely modified. Nodes are generally copied
     * (unless they are immutable).
     */
    // TODO doc
    public static JsonNode copyTree(final JsonNode root, final List<Node> nameNodes,
                                    final INameNodeOverwriteCallback nameNodeOverwriteCallback) {
        return copyOrShadow(root, nameNodes, true, new LinkedList<String>(), null, nameNodeOverwriteCallback, true);
    }

    /**
     * Copy a sub-tree from the given tree, based on a hierarchical specification: a tree of field names.
     * <p/>
     * The returned tree is separate from the original one, i.e. it can be safely modified. Nodes are generally copied
     * (unless they are immutable).
     */
    // TODO doc
    public static JsonNode copyTree(final JsonNode root, final List<Node> nameNodes,
                                    final IJsonObjectCallback objectModifierCallback, final INameNodeOverwriteCallback nameNodeOverwriteCallback) {
        return copyOrShadow(root, nameNodes, true, new LinkedList<String>(), objectModifierCallback,
                nameNodeOverwriteCallback, true);
    }

    /**
     * Copy a sub-tree from the given tree, based on a hierarchical specification: a tree of field names.
     * <p/>
     * The returned tree is separate from the original one, i.e. it can be safely modified. Nodes are generally copied
     * (unless they are immutable).
     */
    // TODO doc
    public static JsonNode copyTree(final JsonNode root, final List<Node> nameNodes,
                                    final IJsonObjectCallback objectModifierCallback) {
        return copyOrShadow(root, nameNodes, true, new LinkedList<String>(), objectModifierCallback, null, true);
    }

    /**
     * Shadow a sub-tree from the given tree, based on a hierarchical specification: a tree of field names.
     * <p/>
     * The returned tree may share container objects with the original one, i.e. changes on either tree may be seen in
     * the other. Depending on the JSON and the filter spec, this can result in dramatically less memory and CPU
     * consumption than copyTree(). However, it's potentially unsafe, particularly under concurrency.
     *
     * @see tv.xrm.jfilter.FilteredTreeCopier#copyTree(com.fasterxml.jackson.databind.JsonNode, java.util.List)
     */
    public static JsonNode shadowTree(final JsonNode root, final List<Node> nameNodes) {
        return copyOrShadow(root, nameNodes, false, new LinkedList<String>(), null, null, true);
    }

    private static JsonNode copyOrShadow(final JsonNode root, final List<Node> nNodes, final boolean copy,
                                         final List<String> currentPath, final IJsonObjectCallback objectModifierCallback,
                                         final INameNodeOverwriteCallback nameNodeCallback, boolean parentNodeIsWildcard) {
        if (root.isObject()) {
            final ObjectNode object = (ObjectNode) root;
            final List<Node> nameNodes;
            if (nameNodeCallback != null) {
                List<Node> overwrittenNameNodes = nameNodeCallback.overwriteNameNodesForCurrentSubtree(object,
                        currentPath);
                if (overwrittenNameNodes != null) {
                    nameNodes = overwrittenNameNodes;
                } else {
                    nameNodes = nNodes;
                }
            } else {
                nameNodes = nNodes;
            }
            if (!nameNodes.isEmpty()) {
                final ObjectNode newObject = object.objectNode();
                currentPath.add(null);
                for (final Node node : nameNodes) {
                    final String name = node.getName();
                    currentPath.set(currentPath.size() - 1, name);
                    if (isNotWildcard(name)) {
                        // specific name
                        final JsonNode child = object.get(name);
                        if (child != null) {
                            JsonNode copyOrShadow = copyOrShadow(child, node.getChildren(), copy, currentPath,
                                    objectModifierCallback, nameNodeCallback, false);
                            if (copyOrShadow != null) {
                                newObject.put(name, copyOrShadow);
                            }
                        }
                    } else {
                        // "all children"
                        final Iterator<Map.Entry<String, JsonNode>> children = object.fields();
                        while (children.hasNext()) {
                            final Map.Entry<String, JsonNode> child = children.next();
                            JsonNode copyOrShadow = copyOrShadow(child.getValue(), node.getChildren(), copy,
                                    currentPath, objectModifierCallback, nameNodeCallback, true);
                            if (copyOrShadow != null) {
                                newObject.put(child.getKey(), copyOrShadow);
                            }
                        }
                    }
                }
                currentPath.remove(currentPath.size() - 1);
                if (objectModifierCallback != null) {
                    objectModifierCallback.postProcessObjectNode(newObject, object, currentPath);
                }
                return newObject;
            } else {
                // no further names, but further objects down this branch - copy/shadow fully
                JsonNode c = deepCopyOrRef(root, copy);
                if (objectModifierCallback != null && c.isObject()) {
                    objectModifierCallback.postProcessObjectNode((ObjectNode) c, object, currentPath);
                }
                return c;
            }
        } else if (root.isArray()) {
            ArrayNode a = (ArrayNode) root;
            ArrayNode newArrayNode = a.arrayNode();

            Iterator<JsonNode> iterator = a.elements();
            while (iterator.hasNext()) {
                JsonNode copyOrShadow = copyOrShadow(iterator.next(), nNodes, copy, currentPath,
                        objectModifierCallback, nameNodeCallback, parentNodeIsWildcard);
                if (copyOrShadow != null && !copyOrShadow.isNull()) {
                    newArrayNode.add(copyOrShadow);
                }
            }
            return newArrayNode;
        } else {
            if (!parentNodeIsWildcard && nNodes != null && !nNodes.isEmpty()) {
                return null;
            }
            // a non-object/non-array is always copied/shadowed fully (names do not apply - our processing stops here)
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
