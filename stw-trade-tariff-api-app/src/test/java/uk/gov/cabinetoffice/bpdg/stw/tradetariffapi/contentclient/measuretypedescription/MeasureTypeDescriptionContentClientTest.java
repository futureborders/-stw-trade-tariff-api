/*
 * Copyright 2022 Crown Copyright (Single Trade Window)
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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.contentclient.measuretypedescription;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.contentclient.ContentApiConfiguration;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.MeasureTypeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;

@ExtendWith(MockitoExtension.class)
public class MeasureTypeDescriptionContentClientTest {

  ApplicationProperties applicationProperties;
  private MockWebServer mockWebServer;
  private MeasureTypeDescriptionContentClient measureTypeDescriptionContentClient;
  @Mock private ContentApiConfiguration contentApiConfiguration;
  private static final String CONTENT_SOURCE = "API";

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    applicationProperties =
        new ApplicationProperties(null, null, contentApiConfiguration, CONTENT_SOURCE);
    measureTypeDescriptionContentClient =
        new MeasureTypeDescriptionContentClient(WebClient.builder().build(), applicationProperties);
  }

  @AfterEach
  @SneakyThrows
  void afterEach() {
    mockWebServer.shutdown();
  }

  @SneakyThrows
  @Test
  void shouldReturnMeasureTypeDescriptions() {
    var response =
        Files.readString(
            Path.of(
                Objects.requireNonNull(
                        getClass()
                            .getClassLoader()
                            .getResource("stubs/content/measuretypedescription/750_EN.json"))
                    .toURI()));

    var expected =
        List.of(
            MeasureTypeDescription.builder()
                .measureTypeId("750")
                .locale(Locale.EN)
                .descriptionOverlay("## Organic products: import control")
                .published(true)
                .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
                .build());

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(response));

    var url = mockWebServer.url("/").toString();
    var baseUrl = url.substring(0, url.lastIndexOf("/"));
    when(contentApiConfiguration.getUrl()).thenReturn(baseUrl);

    Flux<MeasureTypeDescription> result =
        measureTypeDescriptionContentClient.findByMeasureTypeIdInAndLocaleAndPublished(
            List.of("750"), Locale.EN, true);

    StepVerifier.create(result).expectNext(expected.get(0)).verifyComplete();
  }

  @SneakyThrows
  @Test
  void shouldReturnEmptyMeasureTypeDescriptionsWhenNoMatchingFound() {
    List<MeasureTypeDescriptionDTO> resp = List.of();
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(new ObjectMapper().writeValueAsString(resp)));

    var url = mockWebServer.url("/").toString();
    var baseUrl = url.substring(0, url.lastIndexOf("/"));
    when(contentApiConfiguration.getUrl()).thenReturn(baseUrl);

    Flux<MeasureTypeDescription> result =
        measureTypeDescriptionContentClient.findByMeasureTypeIdInAndLocaleAndPublished(
            List.of("355"), Locale.EN, true);

    StepVerifier.create(result).verifyComplete();
  }

  @SneakyThrows
  @Test
  void shouldReturnEmptyMeasureTypeDescriptionsWhenInternalServerError() {
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(500)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    var url = mockWebServer.url("/").toString();
    var baseUrl = url.substring(0, url.lastIndexOf("/"));
    when(contentApiConfiguration.getUrl()).thenReturn(baseUrl);

    Flux<MeasureTypeDescription> result =
        measureTypeDescriptionContentClient.findByMeasureTypeIdInAndLocaleAndPublished(
            List.of("355"), Locale.EN, true);

    StepVerifier.create(result).verifyComplete();
  }
}
