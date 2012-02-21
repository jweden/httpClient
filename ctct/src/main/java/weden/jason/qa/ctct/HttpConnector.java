package weden.jason.qa.ctct;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.apache.http.auth.AuthScope.ANY_PORT;

public class HttpConnector {
    private static final Logger LOG = LogManager.getLogger(HttpConnector.class);
    private static String newLine = System.getProperty("line.separator");

    DefaultHttpClient httpclient;

    protected void initialize() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(schemeRegistry);
        httpclient = new DefaultHttpClient(cm);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope("api.constantcontact.com", ANY_PORT),
                new UsernamePasswordCredentials(System.getProperty("apikey") + "%" + System.getProperty("user"), System.getProperty("password")));
        httpclient.setCredentialsProvider(credsProvider);
    }

    protected HttpResponse doReq(String uri) {
        HttpResponse response = null;
        HttpEntity entity = null;
        try {
            HttpRequestBase httpRequest = new HttpGet(uri);
            if (LOG.isDebugEnabled()) {
                LOG.debug("executing request to: " + httpRequest.getURI());
            }

            response = httpclient.execute(httpRequest);
            entity = response.getEntity();
        } catch (Exception e) {
            LOG.error("Had a problem with executing requesting and grabbing response", e);
        } finally {
            StringBuilder responseBuilder = new StringBuilder();
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                responseBuilder.append(header.getName());
                responseBuilder.append(": ");
                responseBuilder.append(header.getValue());
                responseBuilder.append(newLine);
            }

            try {
                if (entity != null) {
                    long len = entity.getContentLength();
                    if (len != -1 && len < 2) {
                        responseBuilder.append(EntityUtils.toString(entity));
                    } else {
                        BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
                        String line;
                        while ((line = in.readLine()) != null) {
                            responseBuilder.append(line);
                            responseBuilder.append(newLine);
                        }
                    }
                }
            } catch (IOException ex) {
                LOG.error("Problem capturing output", ex);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Response is: " + newLine + responseBuilder);
            }
        }
        return response;
    }

    protected void clientTeardown() {
        httpclient.getConnectionManager().shutdown();
    }
}
