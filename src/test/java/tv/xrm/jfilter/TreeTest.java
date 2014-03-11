package tv.xrm.jfilter;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TreeTest {

    @Test
    public void everythingByNameRoot() {
        final JsonNode root = TestUtil.readSampleJson("sample.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root, Arrays.asList(new FilteredTreeCopier.Node("glossary")));

        assertEquals(root, copy);
    }

    @Test
    public void everythingByNameSublevel() {
        final JsonNode root = TestUtil.readSampleJson("sample.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root,
                Arrays.asList(new FilteredTreeCopier.Node("glossary", new FilteredTreeCopier.Node("title"), new FilteredTreeCopier.Node("GlossDiv"),
                        new FilteredTreeCopier.Node("GlossDiv2")))
        );

        assertEquals(root, copy);
    }

    @Test
    public void wildcardWithSub() throws IOException {
        final JsonNode root = TestUtil.readSampleJson("sample2.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root,
                Arrays.asList(new FilteredTreeCopier.Node("glossary", new FilteredTreeCopier.Node(FilteredTreeCopier.WILDCARD, new FilteredTreeCopier.Node("title"))))
        );

        final JsonNode ref = TestUtil.MAPPER.readValue(TestUtil.q(" {'glossary':{'title':'example glossary','GlossDiv':{'title':'S'},'GlossDiv2':{'title':'S'}}} "), JsonNode.class);

        assertEquals(ref, copy);
    }

    @Test
    public void wildcardWithSubShadow() throws IOException {
        final JsonNode root = TestUtil.readSampleJson("sample2.json");

        final JsonNode copy = FilteredTreeCopier.shadowTree(root,
                Arrays.asList(new FilteredTreeCopier.Node("glossary", new FilteredTreeCopier.Node(FilteredTreeCopier.WILDCARD, new FilteredTreeCopier.Node("title"))))
        );

        final JsonNode ref = TestUtil.MAPPER.readValue(TestUtil.q(" {'glossary':{'title':'example glossary','GlossDiv':{'title':'S'},'GlossDiv2':{'title':'S'}}} "), JsonNode.class);

        assertEquals(ref, copy);
    }

    @Test
    public void everythingByWildcardRoot() {
        final JsonNode root = TestUtil.readSampleJson("sample.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(root, Arrays.asList(new FilteredTreeCopier.Node(FilteredTreeCopier.WILDCARD)));

        assertEquals(root, copy);
    }

    @Test
    public void bigJson() {
        final JsonNode tree = TestUtil.readSampleJson("bigsample.json");

        final List<FilteredTreeCopier.Node> spec = Arrays.asList(new FilteredTreeCopier.Node("glossary", new FilteredTreeCopier.Node("GlossDiv4")));

        final JsonNode copy = FilteredTreeCopier.copyTree(tree, spec);

        assertTrue(copy.at("/glossary/GlossDiv3").isMissingNode());
        assertEquals(tree.at("/glossary/GlossDiv4"), copy.at("/glossary/GlossDiv4"));
        assertTrue(copy.at("/glossary/GlossDiv5").isMissingNode());
    }

    @Test
    public void bigJsonShadow() {
        final JsonNode tree = TestUtil.readSampleJson("bigsample.json");

        final List<FilteredTreeCopier.Node> spec = Arrays.asList(new FilteredTreeCopier.Node("glossary", new FilteredTreeCopier.Node("GlossDiv4")));

        final JsonNode copy = FilteredTreeCopier.shadowTree(tree, spec);

        assertSame("not really shadowed?", tree.at("/glossary/GlossDiv4/GlossList"), copy.at("/glossary/GlossDiv4/GlossList"));

        assertTrue(copy.at("/glossary/GlossDiv3").isMissingNode());
        assertEquals(tree.at("/glossary/GlossDiv4"), copy.at("/glossary/GlossDiv4"));
        assertTrue(copy.at("/glossary/GlossDiv5").isMissingNode());
    }

}
