package com.github.dreamhead.moco.action;

import com.github.dreamhead.moco.HttpMethod;
import com.github.dreamhead.moco.HttpProtocolVersion;
import com.github.dreamhead.moco.Request;
import com.github.dreamhead.moco.Response;
import com.github.dreamhead.moco.dumper.Dumper;
import com.github.dreamhead.moco.dumper.HttpRequestDumper;
import com.github.dreamhead.moco.dumper.HttpResponseDumper;
import com.github.dreamhead.moco.model.DefaultHttpRequest;
import com.github.dreamhead.moco.model.DefaultHttpResponse;
import com.github.dreamhead.moco.model.MessageContent;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActionMonitor {
    private static Logger logger = LoggerFactory.getLogger(ActionMonitor.class);
    private final Dumper<Response> responseDumper = new HttpResponseDumper();
    private final Dumper<Request> requestDumper = new HttpRequestDumper();

    private String toPath(final URI uri) {
        final String path = uri.toString();
        final int index = path.indexOf("?");
        if (index >= 0) {
            return path.substring(0, index);
        }

        return path;
    }

    private Map<String, String[]> asQueries(final List<NameValuePair> queries) {
        final Multimap<String, String> multimap = queries.stream()
                .collect(ImmutableListMultimap.toImmutableListMultimap(
                        NameValuePair::getName, NameValuePair::getValue));

        return multimap.asMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toArray(new String[0])));
    }

    private Map<String, String> asHeaders(final Header[] allHeaders) {
        return Arrays.stream(allHeaders)
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
    }

    public final void postAction(final CloseableHttpResponse response) throws IOException {
        final DefaultHttpResponse dumped = DefaultHttpResponse.builder()
                .withVersion(HttpProtocolVersion.versionOf(response.getProtocolVersion().toString()))
                .withStatus(response.getStatusLine().getStatusCode())
                .withHeaders(asHeaders(response.getAllHeaders()))
                .withContent(MessageContent.content().withContent(response.getEntity().getContent()).build())
                .build();
        logger.info("Action Response: {}\n", responseDumper.dump(dumped));
    }

    public final void preAction(final HttpUriRequest request) {
        final URIBuilder uriBuilder = new URIBuilder(request.getURI());
        final DefaultHttpRequest.Builder builder = DefaultHttpRequest.builder()
                .withVersion(HttpProtocolVersion.versionOf(request.getProtocolVersion().toString()))
                .withUri(toPath(request.getURI()))
                .withQueries(asQueries(uriBuilder.getQueryParams()))
                .withMethod(HttpMethod.valueOf(request.getMethod().toUpperCase()))
                .withHeaders(asHeaders(request.getAllHeaders()));

        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
            try {
                builder.withContent(MessageContent.content()
                                .withContent(entityRequest.getEntity().getContent())
                                .build());
            } catch (IOException e) {
                // IGNORE
            }
        }

        logger.info("Action Request:{}\n", requestDumper.dump(builder.build()));
    }
}
