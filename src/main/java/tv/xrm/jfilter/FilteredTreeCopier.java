package tv.xrm.jfilter;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class FilteredTreeCopier {

    public static final String WILDCARD = null;

    private FilteredTreeCopier() {
    }

    public static JsonNode copy(final JsonNode root, final List<String> names) {
        if (root.isObject()) {
            final ObjectNode object = (ObjectNode) root;

            if (!names.isEmpty()) {
                final ObjectNode newObject = object.objectNode();
                final String name = names.get(0);
                final List<String> poppedNames = pop(names);

                if(name != WILDCARD) {
                    // specific name
                    final JsonNode child = object.get(name);
                    if (child != null) {

                        newObject.put(name, copy(child, poppedNames));
                    }
                } else {
                    // "all children"
                    final Iterator<Map.Entry<String, JsonNode>> children = object.fields();
                    while(children.hasNext()) {
                        final Map.Entry<String, JsonNode> child = children.next();
                        newObject.put(child.getKey(), copy(child.getValue(), poppedNames));
                    }
                }

                return newObject;
            } else {
                // no further names, but further objects down this branch - copy fully
                return root.deepCopy();
            }
        } else {
            // a non-object is always copied fully (names do not apply - our processing stops here)
            return root.deepCopy();
        }
    }

    private static List<String> pop(final List<String> list) {
        return list.subList(1, list.size());
    }


}
