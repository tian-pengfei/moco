package com.github.dreamhead.moco.junit;

import com.github.dreamhead.moco.AbstractMocoStandaloneTest;
import org.apache.hc.core5.http.HttpResponse;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.dreamhead.moco.helper.RemoteTestUtils.remoteUrl;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MocoJunitJsonRestRunnerTest extends AbstractMocoStandaloneTest {
    @Rule
    public MocoJunitRunner runner = MocoJunitRunner.jsonRestRunner(12306, "src/test/resources/rest.json");

    @Test
    public void should_return_expected_message() throws IOException {
        HttpResponse response = helper.postForResponse(remoteUrl("/targets"), "hello");
        assertThat(response.getCode(), is(201));
        assertThat(response.getFirstHeader("Location").getValue(), is("/targets/123"));
    }
}
