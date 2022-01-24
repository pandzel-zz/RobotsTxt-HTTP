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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Default implementation of robots cache.
 */
public class RobotsCacheImpl implements RobotsCache {
  private static final int INITIAL_SIZE = 1000;
  /**
   * Singleton instance of the cache.
   */
  public static final RobotsCacheImpl INSTANCE = new RobotsCacheImpl();

  private final LimitedSizeMap<String, Entry> cache = new LimitedSizeMap<>(INITIAL_SIZE, 
          entry -> !entry.isLocked(), 
          (e1, e2) -> e1.counter - e2.counter, 
          e -> { e.counter = 0; return e; });

  @Override
  public RobotsTxt fetch(CloseableHttpClient httpClient, HttpHost target) {
    String address = getAddress(target);
    Entry robotsTxtEntry = cache.get(address);
    if (robotsTxtEntry != null) {
      return robotsTxtEntry.robotsTxt;
    }
    RobotsTxt robotsTxt = fetchRobotsTxt(httpClient, target);
    cache.put(address, new Entry(robotsTxt));
    return robotsTxt;
  }

  /**
   * Gets max cache size.
   * @return max cache size
   */
  public int getMaxSize() {
    return cache.getMaxSize();
  }

  /**
   * Sets max cache size.
   * @param maxSize max cache size
   */
  public void setMaxSize(int maxSize) {
    cache.setMaxSize(maxSize);
  }

  @Override
  public void enter(String userAgent, Integer crawlDelay, HttpHost target) {
    String address = getAddress(target);
    Entry robotsTxtEntry = cache.get(address);
    synchronized (this) {
    if (robotsTxtEntry == null) {
      return;
    }
    if (crawlDelay == null) {
      return;
    }
    robotsTxtEntry.enter(userAgent, crawlDelay);
    }
  }
  
  @Override
  public void release() {
    cache.clear();
  }

  private RobotsTxt fetchRobotsTxt(CloseableHttpClient httpClient, HttpHost target) {
    HttpGet method = new HttpGet(String.format("%s://%s:%d/robots.txt", target.getSchemeName(), target.getHostName(), target.getPort()));
    try (
            CloseableHttpResponse response = httpClient.execute(method);
            InputStream content = response.getEntity().getContent();) {
      return RobotsTxt.read(content);
    } catch (IOException ex) {
      return null;
    }
  }

  private String getAddress(HttpHost target) {
    return String.format("%s://%s:%d", target.getSchemeName(), getHostAddress(target), target.getPort());
  }

  private String getHostAddress(HttpHost target) {
    if (target.getAddress() != null) {
      return target.getAddress().getHostAddress();
    }
    try {
      return InetAddress.getByName(target.getHostName()).getHostAddress();
    } catch (UnknownHostException ex) {
      return target.getHostName().toLowerCase();
    }
  }

  /**
   * Entry.
   */
  private static class Entry {

    private final Map<String, Gate> gates = new HashMap<>();
    public final RobotsTxt robotsTxt;
    public int counter;

    /**
     * Creates instance of the entry.
     * @param robotsTxt robots txt
     */
    public Entry(RobotsTxt robotsTxt) {
      this.robotsTxt = robotsTxt;
    }

    /**
     * Enters the domain.
     * @param userAgent user agent
     * @param crawlDelay crawl delay
     */
    public synchronized void enter(String userAgent, int crawlDelay) {
      counter++;
      Gate gate = gates.get(userAgent);
      if (gate == null) {
        gate = new Gate(crawlDelay);
        gates.put(userAgent, gate);
      }
      gate.enter();
    }
    
    /**
     * Checks if anything locked.
     * @return <code>true</code> if anything locked.
     */
    public synchronized boolean isLocked() {
      return gates.values().stream().anyMatch(Gate::isLocked);
    }
  }

  /**
   * Gate.
   */
  private static class Gate {

    private final int delay;
    private volatile boolean locked;
    private final LinkedList<Lock> locks = new LinkedList<>();

    /**
     * Creates instance of the gate.
     *
     * @param delay wait delay
     */
    public Gate(int delay) {
      this.delay = delay;
    }

    /**
     * Enters the gate.
     */
    public void enter() {
      Lock lock = createLock();
      if (lock != null) {
        lock.enter();
      }
      block();
    }
    
    /**
     * Checks if anything locked.
     * @return <code>true</code> if any pending lock present.
     */
    public synchronized boolean isLocked() {
      return !locks.isEmpty();
    }

    /**
     * Creates lock.
     *
     * @return lock or <code>null</code>
     */
    private synchronized Lock createLock() {
      if (locked) {
        Lock lock = new Lock(delay);
        locks.addLast(lock);
        return lock;
      } else {
        return null;
      }
    }

    /**
     * Blocks the gate.
     */
    private synchronized void block() {
      locked = true;
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(1000 * delay);
            synchronized (Gate.this) {
              locked = false;
              if (!locks.isEmpty()) {
                Lock lock = locks.pop();
                lock.release();
              }
            }
          } catch (InterruptedException ex) {
          }
        }
      });
      t.setDaemon(true);
      t.start();
    }
  }

  /**
   * Lock.
   */
  private static class Lock {
    private final int delay;

    public Lock(int delay) {
      this.delay = delay;
    }

    /**
     * Enters the gate and stops.
     */
    public synchronized void enter() {
      try {
        wait(1000 * delay);
      } catch (InterruptedException ex) {
      }
    }

    /**
     * Releases stopped lock.
     */
    public synchronized void release() {
      notify();
    }
  }

}
