[![Build Status](https://travis-ci.org/pandzel/RobotsTxt-HTTP.png?branch=master)](https://travis-ci.org/pandzel/RobotsTxt-HTTP)
[![Maven Central](https://img.shields.io/maven-central/v/com.panforge/robots-http.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.panforge%22%20AND%20a:%22robots-http%22)

# RobotsTxt-HTTP
Apache HttpClient wrapper with ability to process and apply [robots.txt](http://www.robotstxt.org/orig.html) file directives.

## Instructions

Building the source code:

* Run 'mvn clean install'

Using in your own project:

* Add dependency to the pom.xml

```xml
  <dependencies>
  ...
    <dependency>
        <groupId>com.panforge</groupId>
        <artifactId>robots-http</artifactId>
        <version>1.0.7</version>
    </dependency>
  ...
  </dependencies>
```

* Use in the code

```java
try (CloseableHttpClient httpClient = new HttpClientWrapper(HttpClients.createSystem())) {
  // use hhtpClient to conduct HTTP communication
}
```


## Requirements

* Java JDK 11 or higher

## Licensing
Copyright 2018 Piotr Andzel

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [LICENSE](LICENSE.txt) file.
