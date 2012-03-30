package weden.jason.qa.ctct;

import akka.actor.TypedActor;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.*;

public class HttpConnector implements IHttpConnector {
    private static final Logger LOG = LogManager.getLogger(HttpConnector.class);
    private static String newLine = System.getProperty("line.separator");

    DefaultHttpClient httpclient;

    public void initialize() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(schemeRegistry);
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(200);
        httpclient = new DefaultHttpClient(cm);
    }

    public HttpResponse sendRequest(String uri, HTTPMethod httpMethod, String... entityBody) throws IOException {
        HttpRequestBase httpRequest = null;
        switch (httpMethod) {
            case GET:
                httpRequest = new HttpGet(uri);
                break;

            case POST:
                httpRequest = new HttpPost(uri);
                if (entityBody.length > 0) {
                    String body = entityBody[0];
                    InputStream is = new ByteArrayInputStream(body.getBytes());
                    InputStreamEntity ie = new InputStreamEntity(is, body.length());
                    ((HttpPost) httpRequest).setEntity(ie);
                }
                break;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("executing request to: " + httpRequest.getURI());
        }

        httpRequest.setHeader("Accept", "application/atom+xml");
        httpRequest.setHeader("Content-Type", "application/atom+xml");

        String userPassToUse = System.getProperty("apikey") + "%" + System.getProperty("user") + ":" +
                System.getProperty("password");
        String encoding = DatatypeConverter.printBase64Binary(userPassToUse.getBytes());
        httpRequest.setHeader("Authorization", "Basic " + encoding);

        return httpclient.execute(httpRequest);
    }

    public Future<HttpResponse> sendRequestFuture(String uri, HTTPMethod httpMethod, String... entityBody) throws IOException {
        return Futures.successful(sendRequest(uri, httpMethod, entityBody), TypedActor.dispatcher());
    }

    protected String getBody(HttpResponse resp) throws IOException {
        HttpEntity entity = resp.getEntity();
        StringBuilder bodyBuilder = new StringBuilder();
        if (entity != null) {
            long len = entity.getContentLength();
            if (len != -1 && len < 2048) {
                bodyBuilder.append(EntityUtils.toString(entity));
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
                String line;
                while ((line = in.readLine()) != null) {
                    bodyBuilder.append(line);
                    bodyBuilder.append(newLine);
                }
            }
        }
        return bodyBuilder.toString();
    }

    public void clientTeardown() {
        httpclient.getConnectionManager().shutdown();
    }
}
