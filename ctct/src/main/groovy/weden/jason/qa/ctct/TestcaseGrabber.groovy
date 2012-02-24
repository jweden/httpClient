package weden.jason.qa.ctct;

/**
 * This is the groovy class which puts the test cases in the external testCases.properties
 * file into a data structure for the java program to use.
 */
public class TestcaseGrabber {
    protected List<Map<String, String>> getTestcases() {
        def testConfig = null;
        try {
            testConfig = new ConfigSlurper().parse(new File('src/main/resources/testCases.properties').toURL())
        } catch (Exception ex) {
            println('Error reading in test case property file' + ex.printStackTrace());
        }
        return testConfig.testCases
    }
}
