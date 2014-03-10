package tv.xrm.jfilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

public class TreeTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void simpleBranchFullyQualified() throws IOException {
        final JsonNode root = readSampleJson();

        final JsonNode copy = FilteredTreeCopier.copy(root, Arrays.asList("glossary", "title"));

        final JsonNode ref = MAPPER.readValue(q(" {'glossary':{'title':'example glossary'}} "), JsonNode.class);

        assertEquals(ref, copy);
    }

    @Test
    public void simpleBranchOpen() throws IOException {
        final JsonNode root = readSampleJson();
        final JsonNode copy = FilteredTreeCopier.copy(root, Arrays.asList("glossary"));

        assertEquals(root, copy);
    }

    @Test
    public void simpleBranchOpen2() throws IOException {
        final JsonNode root = readSampleJson();
        final JsonNode copy = FilteredTreeCopier.copy(root, Arrays.asList("glossary", "GlossDiv", "GlossList", "GlossEntry", "GlossDef", "GlossSeeAlso"));

        final JsonNode ref = MAPPER.readValue(q(" {'glossary':{'GlossDiv':{'GlossList':{'GlossEntry':{'GlossDef':{'GlossSeeAlso':['GML','XML']}}}}}} "), JsonNode.class);

        assertEquals(ref, copy);
    }

    @Test
    public void wildcardBranch() throws IOException {
        final JsonNode root = readSampleJson();
        final JsonNode copy = FilteredTreeCopier.copy(root, Arrays.asList("glossary", "GlossDiv", "GlossList", "GlossEntry", FilteredTreeCopier.WILDCARD, "GlossSeeAlso"));


        final JsonNode ref = MAPPER.readValue(q(" {'glossary':{'GlossDiv':{'GlossList':{'GlossEntry':{'ID':'SGML','SortAs':'SGML','GlossTerm':'Standard Generalized Markup Language','Acronym':'SGML','Abbrev':'ISO 8879:1986','GlossDef':{'GlossSeeAlso':['GML','XML']},'GlossSee':'markup'}}}}} "), JsonNode.class);

        assertEquals(ref, copy);
    }

    private JsonNode readSampleJson() throws IOException {
        URL jsonUrl = Thread.currentThread().getContextClassLoader().getResource("sample.json");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonUrl, JsonNode.class);
    }

    private static String q(String singleQuoted) {
        return singleQuoted.replace('\'', '"');
    }

}
