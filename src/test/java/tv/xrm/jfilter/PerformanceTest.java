package tv.xrm.jfilter;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A few performance experiments.
 */
@Ignore("long-running, non-functional")
public class PerformanceTest {

    private static final int LOOP = 1_000_000;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private final JsonNode tree = TestUtil.readSampleJson("bigsample.json");

    private final List<Node> spec = Arrays.asList(new Node("glossary"));

    @Test
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    public void copyBigSample() {
        for (int i = 0; i < LOOP; i++) {
            assertNotNull(FilteredTreeCopier.copyTree(tree, spec));
        }
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    public void shadowBigSample() {
        for (int i = 0; i < LOOP; i++) {
            assertNotNull(FilteredTreeCopier.shadowTree(tree, spec));
        }
    }


}
