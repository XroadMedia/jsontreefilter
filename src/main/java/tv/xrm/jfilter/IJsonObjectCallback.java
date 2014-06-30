package tv.xrm.jfilter;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface IJsonObjectCallback {

    /**
     * Callback for custom post-processing of the ObjectNode.
     *
     * @param newNode
     * @param existingNode
     * @param currentPath
     */
    void postProcessObjectNode(final ObjectNode newNode, final ObjectNode existingNode, final List<String> currentPath);
}
