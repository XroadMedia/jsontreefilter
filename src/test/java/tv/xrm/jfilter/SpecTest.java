package tv.xrm.jfilter;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpecTest {

    @Test
    public void cleansUp() {
        assertEquals("a.b.c", Spec.cleanup("....a..b......c."));
        assertEquals("", Spec.cleanup("...."));
        assertEquals("a", Spec.cleanup(".a."));
    }

    @Test(expected = IllegalArgumentException.class)
    public void barfsOnNull() {
        Spec.spec().add((String) null);
    }

    @Test
    public void normalises() {
        Spec s = Spec.spec(".b.a", "a.b.a", "", "a.b", "a.ba.");
        assertEquals(Arrays.asList("a.b", "a.ba", "b.a"), s.normalised());
    }

    @Test
    public void normalisesWithWildcards() {
        Spec s = Spec.spec(".b.a", "a.b.a", "a.b.a", "a.b.a", "a.b.b", "a..*.x", "", "a.b", "a.ba.");
        assertEquals(Arrays.asList("a.*.x", "a.b", "a.b.b", "a.ba", "b.a"), s.normalised());
    }

    @Test
    public void convertsToNode() throws IOException {
        Spec s = Spec.spec("glossary.title", "glossary.GlossDiv2.GlossList.GlossEntry.GlossDef");

        List<Node> nodes = s.toNodes();

        final JsonNode tree = TestUtil.readSampleJson("sample2.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(tree, nodes);

        final JsonNode ref = TestUtil.MAPPER.readValue(TestUtil.q("{'glossary':{'title':'example glossary','GlossDiv2':{'GlossList':{'GlossEntry':{'GlossDef':{'para':'A meta-markup language, used to create markup languages such as DocBook.','GlossSeeAlso':['GML','XML']}}}}}} "), JsonNode.class);

        assertEquals(ref, copy);
    }

    @Test
    public void convertsToNodeWithSimpleWildcards() throws IOException {
        Spec s = Spec.spec("glossary.title", "glossary.GlossDiv2.GlossList.GlossEntry.*.para");

        List<Node> nodes = s.toNodes();

        final JsonNode tree = TestUtil.readSampleJson("sample2.json");

        final JsonNode copy = FilteredTreeCopier.copyTree(tree, nodes);

        final JsonNode ref = TestUtil.MAPPER.readValue(TestUtil.q(" {'glossary':{'title':'example glossary','GlossDiv2':{'GlossList':{'GlossEntry':{'ID':'SGML','SortAs':'SGML','GlossTerm':'Standard Generalized Markup Language','Acronym':'SGML','Abbrev':'ISO 8879:1986','GlossDef':{'para':'A meta-markup language, used to create markup languages such as DocBook.'},'GlossDef2':{'para':'A meta-markup language, used to create markup languages such as DocBook.'},'GlossSee':'markup'}}}}} "), JsonNode.class);

        assertEquals(ref, copy);
    }

    @Test
    public void simpleDemo() throws IOException {

        final JsonNode tree = TestUtil.readSampleJson("demosample.json");

        Spec s = Spec.spec("a", "b.c", "b.e.*.nickname");
        final JsonNode copy = FilteredTreeCopier.copyTree(tree, s.toNodes());

        assertEquals(TestUtil.readSampleJson("demosample-expected.json"), copy);

    }

}
