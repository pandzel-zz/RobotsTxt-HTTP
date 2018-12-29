/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panforge.robotstxt.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
/**
 *
 * @author Piotr
 */
public class HttpClientWrapperTest {
  
  private static int MOCK_SERVER_PORT = 1080;
  private static ClientAndServer mockServer;
  
  private CloseableHttpClient httpClient;
  private HttpClientWrapper httpClientWrapper;
  
  public HttpClientWrapperTest() {
  }
  
  @BeforeClass
  public static void setUpClass() throws IOException {
    mockServer = startClientAndServer(MOCK_SERVER_PORT);
    mockServer.when(HttpRequest.request("/robots.txt"))
            .respond(HttpResponse.response().withBody(readRobotsTxt()));
    mockServer.when(HttpRequest.request("/index.html"))
            .respond(HttpResponse.response().withBody(readIndexHtml()));
  }
  
  @AfterClass
  public static void tearDownClass() {
    mockServer.stop();
  }
  
  @Before
  public void setUp() {
    httpClient = HttpClients.createSystem();
    httpClientWrapper = new HttpClientWrapper(httpClient);
  }
  
  @After
  public void tearDown() throws IOException {
    httpClientWrapper.close();
  }

  @Test
  public void testReadingRobotsTxt() throws Exception {
    String content = readContent(makeTestUrl("/robots.txt"), null);
    assertTrue(!content.isEmpty());
  }

  @Test(expected = HttpRobotsException.class)
  public void testAccessingForbiddenPath() throws Exception {
    String content = readContent(makeTestUrl("/root/data.txt"), null);
  }
  
  @Test
  public void testAccessingIndexHtml() throws Exception {
    String content = readContent(makeTestUrl("/index.html"), null);
    assertNotNull(content);
    assertTrue(!content.isEmpty());
  }
  
  @Test
  public void testCrawlDelay() throws Exception {
    long start = Calendar.getInstance().getTimeInMillis();
    readContent("http://localhost:1080/index.html", null);
    readContent("http://localhost:1080/index.html", null);
    long end = Calendar.getInstance().getTimeInMillis();
    assertTrue((end-start)/1000 >= 5);
  }
  
  private String makeTestUrl(String path) {
    return String.format("http://localhost:%d%s", MOCK_SERVER_PORT, path);
  }
  
  private String readContent(String url, String userAgent) throws IOException {
    HttpGet request = new HttpGet(url);
    if (userAgent!=null) {
      request.setHeader("User-Agent", userAgent);
    }
    try ( CloseableHttpResponse response = httpClientWrapper.execute(request); InputStream contentStream = response.getEntity().getContent(); ) {
      String content = IOUtils.toString(contentStream, "UTF-8");
      return content;
    }
  }
  
  private static String readRobotsTxt() throws IOException {
    return readResource("robots.txt");
  }
  
  private static String readIndexHtml() throws IOException {
    return readResource("index.html");
  }
  
  private static String readResource(String resourceName) throws IOException {
    try (
      InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    ) {
      return IOUtils.toString(input, "UTF-8");
    }
  }
}
