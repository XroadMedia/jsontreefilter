package tv.xrm.jfilter;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertNotNull;

/**
 * A few performance experiments.
 */
@Ignore("long-running, non-functional")
public class PerformanceTest {

    private static final int LOOP = 1_000_000;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private final JsonNode tree = TestUtil.readSampleJson("bigsample.json");

    private final List<FilteredTreeCopier.Node> spec = Arrays.asList(new FilteredTreeCopier.Node("glossary"));

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
