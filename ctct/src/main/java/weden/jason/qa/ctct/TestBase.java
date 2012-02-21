package weden.jason.qa.ctct;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class TestBase {
    private static final Logger LOG = LogManager.getLogger(TestBase.class);
    protected HttpConnector httpConn;

    @BeforeClass(description = "Setting up http connection object")
    public void beforeClassSetup() throws Exception {
        httpConn = new HttpConnector();
        httpConn.initialize();
    }


    @AfterClass(description = "tear down")
    public void afterClassTearDown() throws Exception {
        httpConn.clientTeardown();
    }


}

