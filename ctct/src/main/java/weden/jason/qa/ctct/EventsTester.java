package weden.jason.qa.ctct;

import org.apache.http.HttpResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EventsTester extends TestBase {
    private static final Logger LOG = LogManager.getLogger(EventsTester.class);

    @Test(description = "Looking for correct content-type when getting events", invocationCount = 5, threadPoolSize = 5, groups = "fast")
    public void GetEventsAndEnsureContentTypeTest() throws Exception {
        String user = System.getProperty("user");
        HttpResponse resp = httpConn.sendRequest("https://api.constantcontact.com/ws/customers/" + user + "/events",
                HTTPMethod.GET);
        Assert.assertEquals(resp.getHeaders("Content-Type")[0].getValue(), "application/atom+xml",
                "Looking for correct content-type when getting events");
    }

}
