package tv.xrm.jfilter;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

public class BranchTest {

    @Test
    public void simpleBranchFullyQualified() throws IOException {
        final JsonNode root = TestUtil.readSampleJson("sample.json");

        final JsonNode copy = FilteredTreeCopier.copyBranch(root, Arrays.asList("glossary", "title"));

        final JsonNode ref = TestUtil.MAPPER.readValue(TestUtil.q(" {'glossary':{'title':'example glossary'}} "), JsonNode.class);

        assertEquals(ref, copy);
    }

    @Test
    public void simpleBranchOpen() {
        final JsonNode root = TestUtil.readSampleJson("sample.json");
        final JsonNode copy = FilteredTreeCopier.copyBranch(root, Arrays.asList("glossary"));

        assertEquals(root, copy);
    }

    @Test
    public void simpleBranchOpen2() throws IOException {
        final JsonNode root = TestUtil.readSampleJson("sample.json");
        final JsonNode copy = FilteredTreeCopier.copyBranch(root, Arrays.asList("glossary", "GlossDiv", "GlossList", "GlossEntry", "GlossDef", "GlossSeeAlso"));

        final JsonNode ref = TestUtil.MAPPER.readValue(TestUtil.q(" {'glossary':{'GlossDiv':{'GlossList':{'GlossEntry':{'GlossDef':{'GlossSeeAlso':['GML','XML']}}}}}} "), JsonNode.class);

        assertEquals(ref, copy);
    }

    @Test
    public void wildcardBranch() throws IOException {
        final JsonNode root = TestUtil.readSampleJson("sample.json");
        final JsonNode copy = FilteredTreeCopier.copyBranch(root, Arrays.asList("glossary", "GlossDiv", "GlossList", "GlossEntry", FilteredTreeCopier.WILDCARD, "GlossSeeAlso"));

        final JsonNode ref = TestUtil.MAPPER.readValue(TestUtil.q(" {'glossary':{'GlossDiv':{'GlossList':{'GlossEntry':{'ID':'SGML','SortAs':'SGML','GlossTerm':'Standard Generalized Markup Language','Acronym':'SGML','Abbrev':'ISO 8879:1986','GlossDef':{'GlossSeeAlso':['GML','XML']},'GlossSee':'markup'}}}}} "), JsonNode.class);

        assertEquals(ref, copy);
    }

}
