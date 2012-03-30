package weden.jason.qa.ctct;

import akka.actor.ActorContext;
import akka.actor.ActorSystem;
import akka.actor.TypedActor;
import akka.actor.TypedProps;
import akka.dispatch.Await;
import akka.dispatch.Future;
import akka.util.Duration;
import org.apache.http.HttpResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.netty.akka.util.Timeout;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

public class EventsTester extends TestBase {
    private static final Logger LOG = LogManager.getLogger(EventsTester.class);

    @Test(description = "Looking for correct content-type when getting events", invocationCount = 1, threadPoolSize = 1, groups = "fast")
    public void getEventsAndEnsureContentTypeTest() throws Exception {
        String user = System.getProperty("user");
        HttpResponse resp = httpConn.sendRequest("https://api.constantcontact.com/ws/customers/" + user + "/events?pageSize=100",
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

    @Test(description = "Looking for correct content-type when getting events", dataProvider = "queries", groups = "fast")
    public void createEventsTest(final String testDescription, final String xmlToUse) throws Exception {
        String user = System.getProperty("user");
        LOG.info("Testing scenario: " + testDescription);
        HttpResponse resp = httpConn.sendRequest("https://api.constantcontact.com/ws/customers/" + user + "/events",
                HTTPMethod.POST, xmlToUse);
        httpConn.getBody(resp);

        Assert.assertEquals(resp.getStatusLine().getStatusCode(), 201,
                "Looking for success status code for event creation");
    }

    @Test(description = "Looking for correct content-type when getting events", invocationCount = 5, threadPoolSize = 5, groups = "fast")
     public void createConcurrentEventsTest() throws Exception {
        String user = System.getProperty("user");
        final List<Map<String, String>> testCases1 = new TestcaseGrabber().getTestcases();
        HttpResponse resp = httpConn.sendRequest("https://api.constantcontact.com/ws/customers/" + user + "/events",
                HTTPMethod.POST, testCases1.get(0).get("xmlToUse"));
        httpConn.getBody(resp);

        Assert.assertEquals(resp.getStatusLine().getStatusCode(), 201,
                "Looking for success status code for each concurrent event creation");
    }

    @Test(description = "Test using akka concurrency", groups = "fast")
    public void concurrentGetEventTest() throws Exception {
        ActorSystem system = ActorSystem.create("httpWorker");
        IHttpConnector httpConn1 = TypedActor.get(system).typedActorOf(new TypedProps<HttpConnector>(IHttpConnector.class, HttpConnector.class));
        httpConn1.initialize();
        String user = System.getProperty("user");

        final ArrayList<Future<HttpResponse>> futures = new ArrayList<Future<HttpResponse>>();
        for (int x = 1; x < 6; x++) {
            futures.add(httpConn1.sendRequestFuture("https://api.constantcontact.com/ws/customers/" + user + "/events?pageSize=100",
                    HTTPMethod.GET));
        }

        for (Future<HttpResponse> futureEntry : futures) {
            HttpResponse resp = Await.result(futureEntry, akka.util.Duration.create(6, TimeUnit.SECONDS));
            Assert.assertEquals(resp.getStatusLine().getStatusCode(), 200,
                    "Looking for success status code for each concurrent event creation");
        }

        httpConn1.clientTeardown();
        TypedActor.get(system).stop(httpConn1);
    }
}
