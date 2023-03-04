package com.github.dreamhead.moco.handler;

import com.github.dreamhead.moco.HttpRequest;
import com.github.dreamhead.moco.MocoConfig;
import com.github.dreamhead.moco.ResponseHandler;
import com.github.dreamhead.moco.internal.SessionContext;
import com.github.dreamhead.moco.model.MessageContent;
import com.github.dreamhead.moco.resource.ContentResource;
import com.github.dreamhead.moco.resource.Resource;
import com.google.common.net.MediaType;

public class ContentHandler extends AbstractContentResponseHandler {
    private final ContentResource resource;

    public ContentHandler(final ContentResource resource) {
        this.resource = resource;
    }

    public final ContentResource getResource() {
        return resource;
    }

    @Override
    protected final MessageContent responseContent(final SessionContext context) {
        return this.resource.readFor(context);
    }

    @Override
    protected final MediaType getContentType(final HttpRequest request) {
        return resource.getContentType(request);
    }

    @Override
    public final ResponseHandler doApply(final MocoConfig config) {
        Resource appliedResource = this.resource.apply(config);
        if (appliedResource != this.resource) {
            return new ContentHandler((ContentResource) appliedResource);
        }

        return this;
    }
}
