// Copyright 2021 Crown Copyright (Single Trade Window)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.config;

import static java.util.Arrays.stream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.request.PayloadBasedRequestEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.request.RequestEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.response.ResponseEntity;

@Slf4j
public class AppUnderTest {

  private final String host;

  private final Integer port;

  private CloseableHttpClient httpClient;

  private final RequestEntity healthRequest;

  public AppUnderTest(String host, Integer port) {
    this.host = host;
    this.port = port;
    this.httpClient = HttpClients.createDefault();

    healthRequest =
      new RequestEntity() {
        @Override
        public String path() {
          return "/actuator/probes/readinessState";
        }
      };
  }

  public AppUnderTest(String host, Integer port,
    CloseableHttpClient httpClient) {
    this(host, port);
    this.httpClient = httpClient;
  }

  public ResponseEntity health() {
    return send(healthRequest);
  }

  public ResponseEntity send(RequestEntity requestEntity) {
    try {
      RequestBuilder requestBuilder = RequestBuilder.get();
      int modifiedPort =
        requestEntity.path().contains("/actuator/probes/readinessState") ? 9000 : port;
      URI uri = new URIBuilder(requestEntity.path())
        .setScheme("http")
        .setHost(host)
        .setPort(modifiedPort)
        .build();
      log.info("Sending request to {}", uri);
      requestBuilder.setUri(uri);
      if (requestEntity.hasPayload()) {
        requestBuilder.setEntity(
          new StringEntity(((PayloadBasedRequestEntity<?>) requestEntity).payload()));
      }
      requestEntity.getHeaders().forEach(requestBuilder::addHeader);

      return getResponse(requestBuilder.build());
    } catch (UnsupportedEncodingException | URISyntaxException e) {
      log.error("Error creating request", e);
      throw new RuntimeException("Error creating request.");
    }
  }

  private ResponseEntity getResponse(HttpUriRequest request) {
    try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
      return toResponseEntity(httpResponse);
    } catch (IOException e) {
      log.error("App call failed.", e);
      throw new RuntimeException("App call failed.");
    }
  }

  private ResponseEntity toResponseEntity(HttpResponse response) throws IOException {
    HttpEntity entity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();

    String payload =
      Optional.ofNullable(entity)
        .map(
          en -> {
            try {
              return EntityUtils.toString(en);
            } catch (IOException ex) {
              ex.printStackTrace();
              return null;
            }
          })
        .orElse(null);
    EntityUtils.consume(entity);
    return new ResponseEntity(statusCode, payload, headersFrom(response));
  }

  private static Map<String, String> headersFrom(HttpResponse response) {
    return stream(response.getAllHeaders())
      .collect(Collectors.toMap(Header::getName, Header::getValue));
  }
}
