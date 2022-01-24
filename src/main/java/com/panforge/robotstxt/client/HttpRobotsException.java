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

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

/**
 * HTTP robots exception.
 */
public class HttpRobotsException extends HttpResponseException {
  private final String path;
  private final String userAgent;
  private final String clause;
  
  /**
   * Creates instance of the exception.
   * @param path path
   * @param userAgent user agent
   * @param clause clause
   */
  public HttpRobotsException(String path, String userAgent, String clause) {
    super(HttpStatus.SC_FORBIDDEN, String.format("Access to %s denied by robots.txt (clause: %s) for user agent %s", path, clause, userAgent));
    this.path = path;
    this.userAgent = userAgent;
    this.clause = clause;
  }

  /**
   * Gets path.
   * @return path
   */
  public String getPath() {
    return path;
  }

  /**
   * Gets user agent.
   * @return user agent
   */
  public String getUserAgent() {
    return userAgent;
  }

  /**
   * Gets clause.
   * @return clause
   */
  public String getClause() {
    return clause;
  }
  
  
}
