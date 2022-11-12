package com.github.dreamhead.moco;

import org.apache.hc.client5.http.fluent.Request;
import org.junit.Test;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.uri;
import static com.github.dreamhead.moco.Runner.running;
import static com.github.dreamhead.moco.helper.RemoteTestUtils.remoteUrl;
import static com.github.dreamhead.moco.helper.RemoteTestUtils.root;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MocoHttpMethodTest extends AbstractMocoHttpTest {
    @Test
    public void should_match_get_method() throws Exception {
        server.get(by(uri("/foo"))).response("bar");

        running(server, () -> assertThat(helper.get(remoteUrl("/foo")), is("bar")));
    }

    @Test
    public void should_match_post_method() throws Exception {
        server.post(by("foo")).response("bar");

        running(server, () -> assertThat(helper.postContent(root(), "foo"), is("bar")));
    }

    @Test
    public void should_match_put_method() throws Exception {
        server.put(by("foo")).response("bar");

        running(server, () -> {
            Request request = Request.put(root()).bodyByteArray("foo".getBytes());
            assertThat(helper.executeAsString(request), is("bar"));
        });
    }

    @Test
    public void should_match_delete_method() throws Exception {
        server.delete(by(uri("/foo"))).response("bar");

        running(server, () -> {
            Request request = Request.delete(remoteUrl("/foo"));
            String response = helper.executeAsString(request);
            assertThat(response, is("bar"));
        });
    }
}
