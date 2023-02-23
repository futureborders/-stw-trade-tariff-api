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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionContentRepo;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;

@Component
@AllArgsConstructor
@Slf4j
public class DocumentCodeDescriptionContentClient implements DocumentCodeDescriptionContentRepo {

  private static final String DOCUMENT_CODE_DESCRIPTION_API_ENDPOINT =
      "/api/v1/document-code-descriptions";
  private static final String DOCUMENT_CODES = "documentCodes";
  private static final String LOCALE = "locale";
  private final WebClient webClient;
  private final ApplicationProperties applicationProperties;

  public Flux<DocumentCodeDescription> findByDocumentCodeInAndLocaleAndPublished(
      List<String> documentCodes, Locale locale, boolean published) {
    var contentApiConfiguration = applicationProperties.getContentApi();
    log.debug(
        "Calling the content api with baseUrl: {}, api path: {}, documentCodes: {} and locale: {}",
        contentApiConfiguration.getUrl(),
        DOCUMENT_CODE_DESCRIPTION_API_ENDPOINT,
        documentCodes,
        locale);
    final Mono<DocumentCodeDescriptionResponseDTO> resultMono =
        webClient
            .mutate()
            .baseUrl(contentApiConfiguration.getUrl())
            .build()
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(DOCUMENT_CODE_DESCRIPTION_API_ENDPOINT)
                        .queryParam(DOCUMENT_CODES, String.join(",", documentCodes))
                        .queryParam(LOCALE, locale.name())
                        .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(DocumentCodeDescriptionResponseDTO.class)
            .onErrorResume(
                ex -> {
                  log.error(
                      "Error occurred while fetching document codes: {} by locale : {}",
                      documentCodes,
                      locale,
                      ex);
                  return Mono.empty();
                });

    return resultMono.flatMapMany(
        response ->
            Flux.fromIterable(
                response.getDocumentCodeDescriptions().stream()
                    .map(
                        documentCodeDescriptionDTO ->
                            DocumentCodeDescription.builder()
                                .documentCode(documentCodeDescriptionDTO.getDocumentCode())
                                .locale(Locale.valueOf(documentCodeDescriptionDTO.getLocale()))
                                .descriptionOverlay(
                                    documentCodeDescriptionDTO.getDescriptionOverlay())
                                .published(true)
                                .destinationCountryRestrictions(
                                    Set.of(UkCountry.GB, UkCountry.XI))
                                .build())
                    .collect(Collectors.toList())));
  }
}
