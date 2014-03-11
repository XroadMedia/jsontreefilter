package tv.xrm.jfilter;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public final class FilteredTreeCopier {

    public static final String WILDCARD = null;

    public static final class Node {
        private final String name;
        private final List<Node> children;

        public Node(final String name, final List<Node> children) {
            this.name = name;
            this.children = Objects.requireNonNull(children);
        }

        public Node(final String name) {
            this(name, Collections.<Node>emptyList());
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }
    }

    private FilteredTreeCopier() {
    }

    public static JsonNode copyTree(final JsonNode root, final List<Node> nameNodes) {
        if (root.isObject()) {
            final ObjectNode object = (ObjectNode) root;

            if (!nameNodes.isEmpty()) {
                final ObjectNode newObject = object.objectNode();

                for (final Node node : nameNodes) {
                    final String name = node.name;

                    if (name != WILDCARD) {
                        // specific name
                        final JsonNode child = object.get(name);
                        if (child != null) {
                            newObject.put(name, copyTree(child, node.children));
                        }
                    } else {
                        // "all children"
                        final Iterator<Map.Entry<String, JsonNode>> children = object.fields();
                        while (children.hasNext()) {
                            final Map.Entry<String, JsonNode> child = children.next();
                            newObject.put(child.getKey(), copyTree(child.getValue(), node.children));
                        }
                    }
                }

                return newObject;
            } else {
                // no further names, but further objects down this branch - copyBranch fully
                return root.deepCopy();
            }
        } else {
            // a non-object is always copied fully (names do not apply - our processing stops here)
            return root.deepCopy();
        }
    }

    public static JsonNode copyBranch(final JsonNode root, final List<String> names) {
        return copyTree(root, convertStringList(names));
    }

    private static List<Node> convertStringList(final List<String> names) {
        if(names.isEmpty()) {
            return Collections.emptyList();
        } else {
            String current = names.get(0);
            List<String> poppedNames = pop(names);
            return Collections.singletonList(new Node(current, convertStringList(poppedNames)));
        }
    }

    private static List<String> pop(final List<String> list) {
        return list.subList(1, list.size());
    }


}
