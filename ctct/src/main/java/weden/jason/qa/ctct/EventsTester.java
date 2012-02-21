package weden.jason.qa.ctct;

import org.apache.http.HttpResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class EventsTester extends TestBase {
    private static final Logger LOG = LogManager.getLogger(EventsTester.class);

    @Test(description = "Looking for correct content-type when getting events", invocationCount = 5, threadPoolSize = 5, groups = "fast")
    public void getEventsAndEnsureContentTypeTest() throws Exception {
        String user = System.getProperty("user");
        HttpResponse resp = httpConn.sendRequest("https://api.constantcontact.com/ws/customers/" + user + "/events",
                HTTPMethod.GET);
        httpConn.getBody(resp);
        Assert.assertEquals(resp.getHeaders("Content-Type")[0].getValue(), "application/atom+xml",
                "Looking for correct content-type when getting events");
    }

    @Test(description = "Negative test for bad oauth2 login", groups = "fast")
    public void badOauth2Login() throws Exception {
        HttpResponse resp = httpConn.sendRequest("https://oauth2.constantcontact.com/oauth2/oauth/token?" +
                "grant_type=authorization_code&" +
                "client_id=4&" +
                "client_secret=2&" +
                "code=d&" +
                "redirect_uri=https://www.verisign.com",
                HTTPMethod.POST);

        String body = httpConn.getBody(resp);
        Map<String, String> jsonResult = new JSONDecoder().parse(body);

        Assert.assertEquals(jsonResult.get("error"), "invalid_grant", "Verifying error for bad oauth2 login");
        Assert.assertEquals(jsonResult.get("error_description"), "Invalid verification code: d",
                "Verifying error description for bad oauth2 login");
    }
}
