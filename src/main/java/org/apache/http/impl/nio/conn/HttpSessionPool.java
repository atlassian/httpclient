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
package org.apache.http.impl.nio.conn;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.nio.pool.RouteResolver;
import org.apache.http.impl.nio.pool.SessionPool;
import org.apache.http.nio.reactor.ConnectingIOReactor;

class HttpSessionPool extends SessionPool<HttpRoute> {

    public HttpSessionPool(final ConnectingIOReactor ioreactor) {
        super(ioreactor, new InternalRouteResolver(), 20, 50);
    }

    static class InternalRouteResolver implements RouteResolver<HttpRoute> {

        public SocketAddress resolveLocalAddress(final HttpRoute route) {
            return new InetSocketAddress(route.getLocalAddress(), 0);
        }

        public SocketAddress resolveRemoteAddress(final HttpRoute route) {
            HttpHost target = route.getTargetHost();
            return new InetSocketAddress(target.getHostName(), target.getPort());
        }

    }

}