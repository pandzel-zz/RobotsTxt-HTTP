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
import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Robots cache.
 */
public interface RobotsCache {
  /**
   * Fetches cached robots.txt.
   * @param httpClient HTTP client
   * @param target target
   * @return robots.txt if any
   */
  RobotsTxt fetch(CloseableHttpClient httpClient, HttpHost target);
  
  /**
   * Enters into the host.
   * It will stop any further access by any other thread by given crawl delay
   * @param userAgent user agent or <code>null</code> if no user agent specified
   * @param crawlDelay crawl delay or <code>null</code> if no delay specified
   * @param target target
   */
  void enter(String userAgent, Integer crawlDelay, HttpHost target);
  
  /**
   * Releases all cached information.
   */
  void release();
  
  /**
   * Default singleton instance.
   */
  RobotsCache DEFAULT = RobotsCacheImpl.INSTANCE;
}
