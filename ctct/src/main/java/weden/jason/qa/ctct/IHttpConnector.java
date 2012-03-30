package weden.jason.qa.ctct;

import akka.dispatch.Future;
import org.apache.http.HttpResponse;

import java.io.IOException;

public interface IHttpConnector {
    public void initialize();
    public Future<HttpResponse> sendRequestFuture(String uri, HTTPMethod httpMethod, String... entityBody) throws IOException;
    public void clientTeardown();
}
