/*
 * Copyright 2018 Piotr Andzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.panforge.robotstxt.client;

import com.panforge.robotstxt.RobotsTxt;
import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import com.panforge.robotstxt.Grant;

/**
 * Apache HTTP client wrapper with robots.
 */
public class HttpClientWrapper extends CloseableHttpClient {

  private final CloseableHttpClient httpClient;
  private final RobotsCache robotsCache;

  /**
   * Creates instance of the wrapper.
   *
   * @param httpClient HTTP client
   * @param robotsCache robots cache
   */
  public HttpClientWrapper(CloseableHttpClient httpClient, RobotsCache robotsCache) {
    this.httpClient = httpClient;
    this.robotsCache = robotsCache;
  }

  /**
   * Creates instance of the wrapper.
   *
   * @param httpClient underlying HTTP client
   */
  public HttpClientWrapper(CloseableHttpClient httpClient) {
    this(httpClient, RobotsCache.DEFAULT);
  }

  @Override
  protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
    RobotsTxt robotsTxt = !request.getRequestLine().getUri().equals("/robots.txt") ? robotsCache.fetch(httpClient, target) : null;
    if (robotsTxt != null) {
      Header userAgentHeader = request.getFirstHeader("User-Agent");
      String userAgent = userAgentHeader != null ? userAgentHeader.getValue() : "";
      Grant grant = robotsTxt.ask(userAgent, request.getRequestLine().getUri());
      if (!grant.hasAccess()) {
        throw new HttpRobotsException(request.getRequestLine().getUri().toString(), userAgent, grant.getClause());
      }
      if (grant.getCrawlDelay() != null) {
        robotsCache.enter(userAgent, grant.getCrawlDelay(), target);
      }
    }
    return httpClient.execute(target, request, context);
  }

  @Override
  @SuppressWarnings("deprecation")
  public HttpParams getParams() {
    return httpClient.getParams();
  }

  @Override
  @SuppressWarnings("deprecation")
  public ClientConnectionManager getConnectionManager() {
    return httpClient.getConnectionManager();
  }

  @Override
  public void close() throws IOException {
    robotsCache.release();
    httpClient.close();
  }
}
