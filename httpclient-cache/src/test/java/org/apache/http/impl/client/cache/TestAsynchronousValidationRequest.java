/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.impl.client.cache;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.cache.HeaderConstants;
import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class TestAsynchronousValidationRequest {

    private AsynchronousValidator mockParent;
    private CachingHttpClient mockClient;
    private HttpHost target;
    private HttpRequest request;
    private HttpContext mockContext;
    private HttpCacheEntry mockCacheEntry;
    private HttpResponse mockResponse;
    private StatusLine mockStatusLine;

    @Before
    public void setUp() {
        mockParent = EasyMock.createMock(AsynchronousValidator.class);
        mockClient = EasyMock.createNiceMock(CachingHttpClient.class);
        target = new HttpHost("foo.example.com");
        request = new HttpGet("/");
        mockContext = EasyMock.createNiceMock(HttpContext.class);
        mockCacheEntry = EasyMock.createNiceMock(HttpCacheEntry.class);
        mockResponse = EasyMock.createNiceMock(HttpResponse.class);
        mockStatusLine = EasyMock.createNiceMock(StatusLine.class);
    }

    @Test
    public void testRunCallsCachingClientAndRemovesIdentifier() throws ProtocolException, IOException {
        String identifier = "foo";

        AsynchronousValidationRequest asynchRequest = new AsynchronousValidationRequest(
                mockParent, mockClient, target, request, mockContext, mockCacheEntry,
                identifier, 0);

        // response not used
        EasyMock.expect(mockClient.revalidateCacheEntry(target, request, mockContext, mockCacheEntry)).andReturn(mockResponse);
        EasyMock.expect(mockResponse.getStatusLine()).andReturn(mockStatusLine);
        EasyMock.expect(mockStatusLine.getStatusCode()).andReturn(200);
        mockParent.markComplete(identifier);
        mockParent.jobSuccessful(identifier);

        replayMocks();
        asynchRequest.run();
        verifyMocks();
    }

    @Test
    public void testRunReportsJobFailedForServerError() throws ProtocolException, IOException {
        String identifier = "foo";

        AsynchronousValidationRequest asynchRequest = new AsynchronousValidationRequest(
                mockParent, mockClient, target, request, mockContext, mockCacheEntry,
                identifier, 0);

        // response not used
        EasyMock.expect(mockClient.revalidateCacheEntry(target, request, mockContext, mockCacheEntry)).andReturn(mockResponse);
        EasyMock.expect(mockResponse.getStatusLine()).andReturn(mockStatusLine);
        EasyMock.expect(mockStatusLine.getStatusCode()).andReturn(503);
        mockParent.markComplete(identifier);
        mockParent.jobFailed(identifier);

        replayMocks();
        asynchRequest.run();
        verifyMocks();
    }

    @Test
    public void testRunReportsJobFailedForStaleResponse() throws ProtocolException, IOException {
        String identifier = "foo";

        AsynchronousValidationRequest asynchRequest = new AsynchronousValidationRequest(
                mockParent, mockClient, target, request, mockContext, mockCacheEntry,
                identifier, 0);

        // response not used
        EasyMock.expect(mockClient.revalidateCacheEntry(target, request, mockContext, mockCacheEntry)).andReturn(mockResponse);
        EasyMock.expect(mockResponse.getStatusLine()).andReturn(mockStatusLine);
        EasyMock.expect(mockStatusLine.getStatusCode()).andReturn(200);
        EasyMock.expect(mockResponse.getHeaders(HeaderConstants.WARNING)).andReturn(new Header[] {new BasicHeader(HeaderConstants.WARNING, "\"Response is stale\"")});
        mockParent.markComplete(identifier);
        mockParent.jobFailed(identifier);

        replayMocks();
        asynchRequest.run();
        verifyMocks();
    }

    @Test
    public void testRunGracefullyHandlesProtocolException() throws IOException, ProtocolException {
        String identifier = "foo";

        AsynchronousValidationRequest impl = new AsynchronousValidationRequest(
                mockParent, mockClient, target, request, mockContext, mockCacheEntry,
                identifier, 0);

        // response not used
        EasyMock.expect(
                mockClient.revalidateCacheEntry(target, request, mockContext,
                        mockCacheEntry)).andThrow(new ProtocolException());
        mockParent.markComplete(identifier);
        mockParent.jobFailed(identifier);

        replayMocks();
        impl.run();
        verifyMocks();
    }

    @Test
    public void testRunGracefullyHandlesIOException() throws IOException, ProtocolException {
        String identifier = "foo";

        AsynchronousValidationRequest impl = new AsynchronousValidationRequest(
                mockParent, mockClient, target, request, mockContext, mockCacheEntry,
                identifier, 0);

        // response not used
        EasyMock.expect(
                mockClient.revalidateCacheEntry(target, request, mockContext,
                        mockCacheEntry)).andThrow(new IOException());
        mockParent.markComplete(identifier);
        mockParent.jobFailed(identifier);

        replayMocks();
        impl.run();
        verifyMocks();
    }

    @Test
    public void testRunGracefullyHandlesRuntimeException() throws IOException, ProtocolException {
        String identifier = "foo";

        AsynchronousValidationRequest impl = new AsynchronousValidationRequest(
                mockParent, mockClient, target, request, mockContext, mockCacheEntry,
                identifier, 0);

        // response not used
        EasyMock.expect(
                mockClient.revalidateCacheEntry(target, request, mockContext,
                        mockCacheEntry)).andThrow(new RuntimeException());
        mockParent.markComplete(identifier);
        mockParent.jobFailed(identifier);

        replayMocks();
        impl.run();
        verifyMocks();
    }

    public void replayMocks() {
        EasyMock.replay(mockClient);
        EasyMock.replay(mockContext);
        EasyMock.replay(mockCacheEntry);
        EasyMock.replay(mockResponse);
        EasyMock.replay(mockStatusLine);
        EasyMock.replay(mockParent);
    }

    public void verifyMocks() {
        EasyMock.verify(mockClient);
        EasyMock.verify(mockContext);
        EasyMock.verify(mockCacheEntry);
        EasyMock.verify(mockResponse);
        EasyMock.verify(mockStatusLine);
        EasyMock.verify(mockParent);
    }
}
