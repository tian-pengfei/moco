package com.github.dreamhead.moco.resource.reader;

import com.github.dreamhead.moco.HttpRequest;
import com.github.dreamhead.moco.Request;
import com.github.dreamhead.moco.model.MessageContent;
import com.google.common.net.MediaType;

import java.nio.charset.Charset;
import java.util.function.Function;

import static com.github.dreamhead.moco.util.Jsons.toJson;

public final class JsonResourceReader implements ContentResourceReader, FunctionResourceReader {
    private final Function<Request, Object> function;

    public JsonResourceReader(final Function<Request, Object> function) {
        this.function = function;
    }

    @Override
    public MediaType getContentType(final HttpRequest request) {
        return MediaType.create("application", "json").withCharset(Charset.defaultCharset());
    }

    @Override
    public MessageContent readFor(final Request request) {
        return read(this.function, request);
    }

    public Object getPojo() {
        return function.apply(null);
    }

    @Override
    public MessageContent defaultRead(final Object value) {
        return MessageContent.content().withContent(toJson(value)).build();
    }
}
