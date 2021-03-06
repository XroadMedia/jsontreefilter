package tv.xrm.jfilter;


import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NodeTest {

    @Test(expected = NullPointerException.class)
    public void barfsOnNullName() {
        new Node(null);
    }

    @Test
    public void getsChildren() {
        final List<Node> kids = Arrays.asList(new Node("foo"), new Node("bar"), new Node("baz"));

        Node n = new Node("bla", kids);

        assertEquals("bla", n.getName());
        assertEquals(kids, n.getChildren());
    }

    @Test
    public void deepCopy() {
        final List<Node> kids = Arrays.asList(new Node("foo", Arrays.asList(new Node("bar"), new Node("baz"))));

        Node n = new Node("bla", kids);
        Node deepCopy = n.deepCopy();

        assertEquals("bla", deepCopy.getName());
        Node secondLevel = deepCopy.getChildren().get(0);
        assertEquals("foo", secondLevel.getName());
        Node secondOnThirdLevel = secondLevel.getChildren().get(1);
        assertEquals("baz", secondOnThirdLevel.getName());
    }

    @Test
    public void toStringWorks() {
        final List<Node> kids = Arrays.asList(new Node("foo"), new Node("bar"), new Node("baz"));

        Node n = new Node("bla", kids);

        assertEquals("'bla'('foo'() 'bar'() 'baz'())", n.toString());
    }

}
