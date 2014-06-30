package tv.xrm.jfilter;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface INameNodeOverwriteCallback {

    /**
     * Callback method to overwrite node spec for current subtree dynamically (eg based on a attribute value of the
     * existing node). must return null in case there is nothing to overwrite.
     *
     * @param currentPath
     * @return name node list or null if there is no overwrite needed
     */
    List<Node> overwriteNameNodesForCurrentSubtree(final ObjectNode existingNode, final List<String> currentPath);
}
