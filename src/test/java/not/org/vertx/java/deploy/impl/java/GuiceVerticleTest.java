package not.org.vertx.java.deploy.impl.java;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.junit4.support.RunInVertx;
import org.vertx.junit4.support.annotations.Verticle;
import org.vertx.junit4.support.annotations.Verticles;

import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.assertEquals;

public class GuiceVerticleTest {
    @Rule
    public RunInVertx vertxRule = new RunInVertx();
    private static final String MESSAGE = "this an injected message";

    private static final int VERTX_PORT = 8383;

    @Test
    @Verticles(
        @Verticle(
            value = "guiceModule:not.org.vertx.java.deploy.impl.java.GuiceVerticleTest$MyGuiceModule",
            type = Verticle.Type.GUICE
        )
    )
    public void testRun() throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        try {
            final HttpGet httpget = new HttpGet("http://localhost:" + VERTX_PORT + "/");
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);
            assertEquals(MESSAGE, responseBody);
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }


    public static class MyGuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class)
                    .annotatedWith(Names.named("description"))
                    .toInstance(MESSAGE);
            bind(org.vertx.java.deploy.Verticle.class).to(GuiceVerticle.class);

        }
    }

    public static class GuiceVerticle extends org.vertx.java.deploy.Verticle {

        public String description;

        @Inject
        public GuiceVerticle(@Named("description") String description) {
            this.description = description;
        }

        @Override
        public void start() throws Exception {
            vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
                @Override
                public void handle(HttpServerRequest req) {
                    req.response.setChunked(true).write(description).end();
                }
            }).listen(VERTX_PORT);

        }
    }
}
