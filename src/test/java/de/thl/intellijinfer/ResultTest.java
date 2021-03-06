package de.thl.intellijinfer;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import de.thl.intellijinfer.model.InferBug;
import de.thl.intellijinfer.service.ResultParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ResultTest extends LightPlatformCodeInsightFixtureTestCase {

    private ResultParser rp;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.rp = ResultParser.getInstance(getProject());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/";
    }

    public void testParseSingleBug() {
        final Path jsonPath = Paths.get(getTestDataPath() + "singleBug.json");

        Map<String, List<InferBug>> bugList = this.rp.parse(jsonPath);
        assertNotNull(bugList);
        assertEquals(1, bugList.size());

        final List<InferBug> bugs = bugList.values().stream().findFirst().get();
        assertEquals("NULL_DEREFERENCE", bugs.get(0).getBugType());
        assertEquals(12, bugs.get(0).getLine());

        assertEquals(3, bugs.get(0).getBugTrace().size());
        assertEquals(11, bugs.get(0).getBugTrace().get(1).getLine());
    }

    public void testParseMultipleBugs() {
        final Path jsonPath = Paths.get(getTestDataPath() + "multipleBugs.json");

        Map<String, List<InferBug>> bugList = this.rp.parse(jsonPath);

        assertNotNull(bugList);
        assertEquals(2, bugList.size());
        assertTrue(bugList.containsKey("main.c"));
        assertTrue(bugList.containsKey("test.c"));

        final List<InferBug> bugs = bugList.values().stream().findFirst().get();
        assertEquals(9, bugs.size());
    }

    public void testEmptyBugs() {
        final Path jsonPath = Paths.get(getTestDataPath() + "emptyBugs.json");

        Map<String, List<InferBug>> bugList = this.rp.parse(jsonPath);

        assertNotNull(bugList);
        assertEquals(0, bugList.size());
    }

}
