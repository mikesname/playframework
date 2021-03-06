/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package play.routing;

import org.junit.Test;
import play.mvc.PathBindable;
import play.mvc.Result;

import java.io.InputStream;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static play.test.Helpers.*;
import static play.mvc.Results.ok;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * This class is in the integration tests so that we have the right helper classes to build a request with to test it.
 */
public abstract class AbstractRoutingDslTest {

    abstract RoutingDsl routingDsl();

    private Router router(Function<RoutingDsl, Router> function) {
        return function.apply(routingDsl());
    }

    @Test
    public void noParameters() {
        Router router = router(routingDsl ->
            routingDsl.GET("/hello/world").routeTo(() -> ok("Hello world")).build()
        );

        assertThat(makeRequest(router, "GET", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "GET", "/foo/bar"));
    }

    @Test
    public void oneParameter() {
        Router router = router(routingDsl ->
            routingDsl.GET("/hello/:to").routeTo(to -> ok("Hello " + to)).build()
        );

        assertThat(makeRequest(router, "GET", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "GET", "/foo/bar"));
    }

    @Test
    public void twoParameters() {
        Router router = router(routingDsl ->
            routingDsl.GET("/:say/:to").routeTo((say, to) -> ok(say + " " + to)).build()
        );

        assertThat(makeRequest(router, "GET", "/Hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "GET", "/foo"));
    }

    @Test
    public void threeParameters() {
        Router router = router(routingDsl ->
            routingDsl.GET("/:say/:to/:extra").routeTo((say, to, extra) -> ok(say + " " + to + extra)).build()
        );

        assertThat(makeRequest(router, "GET", "/Hello/world/!"), equalTo("Hello world!"));
        assertNull(makeRequest(router, "GET", "/foo/bar"));
    }

    @Test
    public void noParametersAsync() {
        Router router = router(routingDsl ->
            routingDsl.GET("/hello/world").routeAsync(() -> completedFuture(ok("Hello world"))).build()
        );

        assertThat(makeRequest(router, "GET", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "GET", "/foo/bar"));
    }

    @Test
    public void oneParameterAsync() {
        Router router = router(routingDsl ->
            routingDsl.GET("/hello/:to").routeAsync(to -> completedFuture(ok("Hello " + to))).build()
        );

        assertThat(makeRequest(router, "GET", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "GET", "/foo/bar"));
    }

    @Test
    public void twoParametersAsync() {
        Router router = router(routingDsl ->
            routingDsl.GET("/:say/:to").routeAsync((say, to) -> completedFuture(ok(say + " " + to))).build()
        );

        assertThat(makeRequest(router, "GET", "/Hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "GET", "/foo"));
    }

    @Test
    public void threeParametersAsync() {
        Router router = router(routingDsl ->
            routingDsl
                .GET("/:say/:to/:extra")
                .routeAsync((say, to, extra) -> completedFuture(ok(say + " " + to + extra)))
                .build()
        );

        assertThat(makeRequest(router, "GET", "/Hello/world/!"), equalTo("Hello world!"));
        assertNull(makeRequest(router, "GET", "/foo/bar"));
    }

    @Test
    public void get() {
        Router router = router(routingDsl ->
            routingDsl.GET("/hello/world").routeTo(() -> ok("Hello world")).build()
        );

        assertThat(makeRequest(router, "GET", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "POST", "/hello/world"));
    }

    @Test
    public void head() {
        Router router = router(routingDsl ->
            routingDsl.HEAD("/hello/world").routeTo(() -> ok("Hello world")).build()
        );

        assertThat(makeRequest(router, "HEAD", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "POST", "/hello/world"));
    }

    @Test
    public void post() {
        Router router = router(routingDsl ->
            routingDsl.POST("/hello/world").routeTo(() -> ok("Hello world")).build()
        );

        assertThat(makeRequest(router, "POST", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "GET", "/hello/world"));
    }

    @Test
    public void put() {
        Router router = router(routingDsl ->
            routingDsl.PUT("/hello/world").routeTo(() -> ok("Hello world")).build()
        );

        assertThat(makeRequest(router, "PUT", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "POST", "/hello/world"));
    }

    @Test
    public void delete() {
        Router router = router(routingDsl ->
            routingDsl.DELETE("/hello/world").routeTo(() -> ok("Hello world")).build()
        );

        assertThat(makeRequest(router, "DELETE", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "POST", "/hello/world"));
    }

    @Test
    public void patch() {
        Router router = router(routingDsl ->
            routingDsl.PATCH("/hello/world").routeTo(() -> ok("Hello world")).build()
        );

        assertThat(makeRequest(router, "PATCH", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "POST", "/hello/world"));
    }

    @Test
    public void options() {
        Router router = router(routingDsl ->
            routingDsl.OPTIONS("/hello/world").routeTo(() -> ok("Hello world")).build()
        );

        assertThat(makeRequest(router, "OPTIONS", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "POST", "/hello/world"));
    }

    @Test
    public void starMatcher() {
        Router router = router(routingDsl ->
            routingDsl.GET("/hello/*to").routeTo((to) -> ok("Hello " + to)).build()
        );

        assertThat(makeRequest(router, "GET", "/hello/blah/world"), equalTo("Hello blah/world"));
        assertNull(makeRequest(router, "GET", "/foo/bar"));
    }

    @Test
    public void regexMatcher() {
        Router router = router(routingDsl ->
            routingDsl.GET("/hello/$to<[a-z]+>").routeTo((to) -> ok("Hello " + to)).build()
        );

        assertThat(makeRequest(router, "GET", "/hello/world"), equalTo("Hello world"));
        assertNull(makeRequest(router, "GET", "/hello/10"));
    }

    @Test
    public void multipleRoutes() {
        Router router = router(routingDsl ->
            routingDsl
                .GET("/hello/:to").routeTo((to) -> ok("Hello " + to))
                .GET("/foo/bar").routeTo(() -> ok("foo bar"))
                .POST("/hello/:to").routeTo((to) -> ok("Post " + to))
                .GET("/*path").routeTo((path) -> ok("Path " + path))
                .build()
        );

        assertThat(makeRequest(router, "GET", "/hello/world"), equalTo("Hello world"));
        assertThat(makeRequest(router, "GET", "/foo/bar"), equalTo("foo bar"));
        assertThat(makeRequest(router, "POST", "/hello/world"), equalTo("Post world"));
        assertThat(makeRequest(router, "GET", "/something/else"), equalTo("Path something/else"));
    }

    @Test
    public void encoding() {
        Router router = router(routingDsl ->
            routingDsl
                .GET("/simple/:to").routeTo((to) -> ok("Simple " + to))
                .GET("/path/*to").routeTo((to) -> ok("Path " + to))
                .GET("/regex/$to<.*>").routeTo((to) -> ok("Regex " + to))
                .build()
        );

        assertThat(makeRequest(router, "GET", "/simple/dollar%24"), equalTo("Simple dollar$"));
        assertThat(makeRequest(router, "GET", "/path/dollar%24"), equalTo("Path dollar%24"));
        assertThat(makeRequest(router, "GET", "/regex/dollar%24"), equalTo("Regex dollar%24"));
    }

    @Test
    public void typed() {
        Router router = router(routingDsl ->
            routingDsl
                .GET("/:a/:b/:c").routeTo((Integer a, Boolean b, String c) ->
                    ok("int " + a + " boolean " + b + " string " + c)
                ).build()
        );

        assertThat(makeRequest(router, "GET", "/20/true/foo"), equalTo("int 20 boolean true string foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongNumberOfParameters() {
        routingDsl().GET("/:a/:b").routeTo(foo -> ok(foo.toString()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void badParameterType() {
        routingDsl().GET("/:a").routeTo((InputStream is) -> ok());
    }

    @Test
    public void bindError() {
        Router router = router(routingDsl ->
            routingDsl.GET("/:a").routeTo((Integer a) -> ok("int " + a)).build()
        );

        assertThat(makeRequest(router, "GET", "/foo"),
                equalTo("Cannot parse parameter a as Int: For input string: \"foo\""));
    }

    @Test
    public void customPathBindable() {
        Router router = router(routingDsl ->
            routingDsl.GET("/:a").routeTo((MyString myString) -> ok(myString.value)).build()
        );

        assertThat(makeRequest(router, "GET", "/foo"), equalTo("a:foo"));
    }

    public static class MyString implements PathBindable<MyString> {
        final String value;

        public MyString() {
            this.value = null;
        }

        public MyString(String value) {
            this.value = value;
        }

        public MyString bind(String key, String txt) {
            return new MyString(key + ":" + txt);
        }

        public String unbind(String key) {
            return null;
        }

        public String javascriptUnbind() {
            return null;
        }
    }

    private String makeRequest(Router router, String method, String path) {
        Result result = routeAndCall(router, fakeRequest(method, path));
        if (result == null) {
            return null;
        } else {
            return contentAsString(result);
        }
    }

}
