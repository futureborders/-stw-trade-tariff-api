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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.contentclient.documentcodedescription;

import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale.EN;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType.EXPORT;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType.IMPORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.contentclient.ContentApiConfiguration;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@ExtendWith(MockitoExtension.class)
class DocumentCodeDescriptionContentClientTest {

  ApplicationProperties applicationProperties;
  private MockWebServer mockWebServer;
  private DocumentCodeDescriptionRepository documentCodeDescriptionRepository;
  @Mock private ContentApiConfiguration contentApiConfiguration;

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    applicationProperties = new ApplicationProperties(null, contentApiConfiguration);
    documentCodeDescriptionRepository =
        new DocumentCodeDescriptionContentClient(
            WebClient.builder().build(), applicationProperties);
  }

  @AfterEach
  @SneakyThrows
  void afterEach() {
    mockWebServer.shutdown();
  }

  @SneakyThrows
  @Test
  void shouldReturnImportSpecificDocumentCodeDescriptions() {
    var response =
        Files.readString(
            Path.of(
                Objects.requireNonNull(
                        getClass()
                            .getClassLoader()
                            .getResource(
                                "stubs/content/documentcodedescription/9111_9100_IMPORT_EN.json"))
                    .toURI()));

    var expected =
        List.of(
            DocumentCodeDescription.builder()
                .documentCode("9111")
                .locale(EN)
                .descriptionOverlay(
                    "[Read about precursor chemical licensing](https://www.gov.uk/guidance/precursor-chemical-licensing)")
                .build(),
            DocumentCodeDescription.builder()
                .documentCode("9100")
                .locale(EN)
                .descriptionOverlay(
                    "You need an import licence if your **firearms and ammunition** are:\r\n"
                        + "- within either chapter 93 (arms and ammunition) or chapter 97 (works of art, collectors' pieces and antiques) of the UK tariff")
                .build());

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(response));

    var url = mockWebServer.url("/").toString();
    var baseUrl = url.substring(0, url.lastIndexOf("/"));
    when(contentApiConfiguration.getUrl()).thenReturn(baseUrl);

    Flux<DocumentCodeDescription> result =
        documentCodeDescriptionRepository.findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
            Arrays.asList("9111", "9100"), IMPORT, EN);

    StepVerifier.create(result).expectNext(expected.get(0), expected.get(1)).verifyComplete();
  }

  @SneakyThrows
  @Test
  void shouldReturnExportSpecificDocumentCodeDescriptions() {
    var response =
        Files.readString(
            Path.of(
                Objects.requireNonNull(
                        getClass()
                            .getClassLoader()
                            .getResource(
                                "stubs/content/documentcodedescription/9111_9100_EXPORT_EN.json"))
                    .toURI()));

    var expected =
        List.of(
            DocumentCodeDescription.builder()
                .documentCode("9111")
                .locale(EN)
                .descriptionOverlay(
                    "Export [Read about precursor chemical licensing](https://www.gov.uk/guidance/precursor-chemical-licensing)")
                .build(),
            DocumentCodeDescription.builder()
                .documentCode("9100")
                .locale(EN)
                .descriptionOverlay(
                    "Export You need an import licence if your **firearms and ammunition** are:\r\n"
                        + "- within either chapter 93 (arms and ammunition) or chapter 97 (works of art, collectors' pieces and antiques) of the UK tariff")
                .build());

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(response));

    var url = mockWebServer.url("/").toString();
    var baseUrl = url.substring(0, url.lastIndexOf("/"));
    when(contentApiConfiguration.getUrl()).thenReturn(baseUrl);

    Flux<DocumentCodeDescription> result =
        documentCodeDescriptionRepository.findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
            Arrays.asList("9111", "9100"), EXPORT, EN);

    StepVerifier.create(result).expectNext(expected.get(0), expected.get(1)).verifyComplete();
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource(
      "uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.utils.StubDataUtils#tradeTypeAndLocales")
  void shouldReturnEmptyDocumentCodeDescriptionsWhenNoMatchingFound(
      TradeType tradeType, Locale locale) {
    List<DocumentCodeDescriptionDTO> resp = List.of();
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(new ObjectMapper().writeValueAsString(resp)));

    var url = mockWebServer.url("/").toString();
    var baseUrl = url.substring(0, url.lastIndexOf("/"));
    when(contentApiConfiguration.getUrl()).thenReturn(baseUrl);

    Flux<DocumentCodeDescription> result =
        documentCodeDescriptionRepository.findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
            Arrays.asList("9111", "9100"), tradeType, locale);

    StepVerifier.create(result).verifyComplete();
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource(
      "uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.utils.StubDataUtils#tradeTypeAndLocales")
  void shouldReturnEmptyDocumentCodeDescriptionsWhenInternalServerError(
      TradeType tradeType, Locale locale) {
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(500)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    var url = mockWebServer.url("/").toString();
    var baseUrl = url.substring(0, url.lastIndexOf("/"));
    when(contentApiConfiguration.getUrl()).thenReturn(baseUrl);

    Flux<DocumentCodeDescription> result =
        documentCodeDescriptionRepository.findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
            Arrays.asList("9111", "9100"), tradeType, locale);

    StepVerifier.create(result).verifyComplete();
  }
}
