package tv.xrm.jfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TreeTest {

    @Test
    public void everythingByNameRoot() {
        final JsonNode root = TestUtil.readSampleJson("sample.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root, Arrays.asList(new Node("glossary")));

        assertEquals(root, copy);
    }

    @Test
    public void everythingByNameSublevel() {
        final JsonNode root = TestUtil.readSampleJson("sample.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root,
                Arrays.asList(new Node("glossary", new Node("title"), new Node("GlossDiv"), new Node("GlossDiv2"))));

        assertEquals(root, copy);
    }
    
    @Test
    public void jsonTreeDoesntHaveTheSameStructure() {
        final JsonNode root = TestUtil.readSampleJson("sample.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root,
                Arrays.asList(new Node("glossary", new Node("title", new Node("nonExisting")), new Node("GlossDiv"))));

        assertTrue(copy.has("glossary"));
        JsonNode glossaryNode = copy.get("glossary");
        assertTrue(glossaryNode.has("GlossDiv"));
        assertFalse(glossaryNode.has("title"));
    }

    @Test
    public void nameNodeOverwrite() {
        final JsonNode root = TestUtil.readSampleJson("sample.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root,
                Arrays.asList(new Node("glossary", new Node("GlossDiv", new Node("GlossList")))),
                new INameNodeOverwriteCallback() {
                    @Override
                    public List<Node> overwriteNameNodesForCurrentSubtree(ObjectNode existingNode,
                            List<String> currentPath) {
                        if (existingNode.has("title") && currentPath.get(currentPath.size() - 1).equals("GlossDiv")) {
                            return Arrays.asList(new Node("title"));
                        }
                        return null;
                    }
                });

        JsonNode glossDivNode = copy.get("glossary").get("GlossDiv");
        assertEquals("S", glossDivNode.get("title").asText());
        assertFalse(glossDivNode.has("GlossList"));
    }

    @Test
    public void customPostProcessing() {
        final JsonNode root = TestUtil.readSampleJson("sample.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root,
                Arrays.asList(new Node("glossary", new Node("GlossDiv"))),
                new IJsonObjectCallback() {
                    @Override
                    public void postProcessObjectNode(ObjectNode newNode, ObjectNode existingNode,
                            List<String> currentPath) {
                        if (currentPath.size() != 2) {
                            return;
                        }
                        if (currentPath.get(0).equals("glossary") && currentPath.get(1).equals("GlossDiv")) {
                            newNode.put("GlossList", 1);
                        }
                    }
                }, null);

        JsonNode glossDivNode = copy.get("glossary").get("GlossDiv");
        assertEquals("S", glossDivNode.get("title").asText());
        assertTrue(glossDivNode.has("GlossList"));
        assertEquals(1, glossDivNode.get("GlossList").asInt());
    }

    @Test
    public void wildcardWithSub() throws IOException {
        final JsonNode root = TestUtil.readSampleJson("sample2.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root,
                Arrays.asList(new Node("glossary", new Node(FilteredTreeCopier.WILDCARD, new Node("title")))));

        final JsonNode ref = TestUtil.MAPPER.readValue(TestUtil
                .q(" {'glossary':{'title':'example glossary','GlossDiv':{'title':'S'},'GlossDiv2':{'title':'S'}}} "),
                JsonNode.class);

        assertEquals(ref, copy);
    }

    @Test
    public void wildcardWithSubShadow() throws IOException {
        final JsonNode root = TestUtil.readSampleJson("sample2.json");

        final JsonNode copy = FilteredTreeCopier.shadowTree(root,
                Arrays.asList(new Node("glossary", new Node(FilteredTreeCopier.WILDCARD, new Node("title")))));

        final JsonNode ref = TestUtil.MAPPER.readValue(TestUtil
                .q(" {'glossary':{'title':'example glossary','GlossDiv':{'title':'S'},'GlossDiv2':{'title':'S'}}} "),
                JsonNode.class);

        assertEquals(ref, copy);
    }

    @Test
    public void everythingByWildcardRoot() {
        final JsonNode root = TestUtil.readSampleJson("sample.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root, Arrays.asList(new Node(FilteredTreeCopier.WILDCARD)));

        assertEquals(root, copy);
    }

    @Test
    public void bigJson() {
        final JsonNode tree = TestUtil.readSampleJson("bigsample.json");

        final List<Node> spec = Arrays.asList(new Node("glossary", new Node("GlossDiv4")));

        final JsonNode copy = FilteredTreeCopier.copyTree(tree, spec);

        assertTrue(copy.at("/glossary/GlossDiv3").isMissingNode());
        assertEquals(tree.at("/glossary/GlossDiv4"), copy.at("/glossary/GlossDiv4"));
        assertTrue(copy.at("/glossary/GlossDiv5").isMissingNode());
    }

    @Test
    public void bigJsonShadow() {
        final JsonNode tree = TestUtil.readSampleJson("bigsample.json");

        final List<Node> spec = Arrays.asList(new Node("glossary", new Node("GlossDiv4")));

        final JsonNode copy = FilteredTreeCopier.shadowTree(tree, spec);

        assertSame("not really shadowed?", tree.at("/glossary/GlossDiv4/GlossList"),
                copy.at("/glossary/GlossDiv4/GlossList"));

        assertTrue(copy.at("/glossary/GlossDiv3").isMissingNode());
        assertEquals(tree.at("/glossary/GlossDiv4"), copy.at("/glossary/GlossDiv4"));
        assertTrue(copy.at("/glossary/GlossDiv5").isMissingNode());
    }

}
