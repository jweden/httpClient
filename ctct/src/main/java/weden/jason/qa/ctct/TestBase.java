package weden.jason.qa.ctct;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import java.util.List;
import java.util.Map;

public class TestBase {
    private static final Logger LOG = LogManager.getLogger(TestBase.class);
    protected HttpConnector httpConn;

    @BeforeClass(description = "Setting up http connection object")
    public void setup() throws Exception {
        httpConn = new HttpConnector();
        httpConn.initialize();
    }

    @DataProvider(name = "queries")
    public Object[][] obtainTestCasesDataProvider() {
        final List<Map<String, String>> testCases = new TestcaseGrabber().getTestcases();

        final Object[][] dataProvider = new Object[testCases.size()][2];
        int outerArrayIndex = 0;

        for (final Map<String, String> testCase : testCases) {
            final Object[] innerArray = new Object[2];
            LOG.info(testCase.get("testDescription"));
            innerArray[0] = testCase.get("testDescription");
            innerArray[1] = testCase.get("xmlToUse");

            dataProvider[outerArrayIndex] = innerArray;
            outerArrayIndex++;
        }

        return dataProvider;
    }

    @AfterClass(description = "Gracefully shut down http connection")
    public void tearDown() throws Exception {
        httpConn.clientTeardown();
    }


}

