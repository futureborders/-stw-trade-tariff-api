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
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.MeasureTypeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.MeasureTypeDescriptionContentRepo;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;

@Component
@AllArgsConstructor
@Slf4j
public class MeasureTypeDescriptionContentClient implements MeasureTypeDescriptionContentRepo {
  private static final String MEASURE_TYPE_DESCRIPTIONS_API_ENDPOINT =
      "/api/v1/measure-type-descriptions";
  private static final String MEASURE_TYPES = "measureTypes";
  private static final String LOCALE = "locale";
  private final WebClient webClient;
  private final ApplicationProperties applicationProperties;

  @Override
  public Flux<MeasureTypeDescription> findByMeasureTypeIdInAndLocaleAndPublished(
      List<String> measureTypes, Locale locale, boolean published) {
    var contentApiConfiguration = applicationProperties.getContentApi();
    log.debug(
        "Calling the content api with baseUrl: {}, api path: {}, measureTypes: {} and locale: {}",
        contentApiConfiguration.getUrl(),
        MEASURE_TYPE_DESCRIPTIONS_API_ENDPOINT,
        measureTypes,
        locale);
    final Mono<MeasureTypeDescriptionResponseDTO> resultMono =
        webClient
            .mutate()
            .baseUrl(contentApiConfiguration.getUrl())
            .build()
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(MEASURE_TYPE_DESCRIPTIONS_API_ENDPOINT)
                        .queryParam(MEASURE_TYPES, String.join(",", measureTypes))
                        .queryParam(LOCALE, locale.name())
                        .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(MeasureTypeDescriptionResponseDTO.class)
            .onErrorResume(
                ex -> {
                  log.error(
                      "Error occurred while fetching measure type descriptions and measure types : {} by locale : {}",
                      measureTypes,
                      locale,
                      ex);
                  return Mono.empty();
                });

    return resultMono.flatMapMany(
        response ->
            Flux.fromIterable(
                response.getMeasureTypeDescriptions().stream()
                    .map(
                        measureTypeDescriptionDTO ->
                            MeasureTypeDescription.builder()
                                .measureTypeId(measureTypeDescriptionDTO.getMeasureType())
                                .locale(Locale.valueOf(measureTypeDescriptionDTO.getLocale()))
                                .descriptionOverlay(
                                    measureTypeDescriptionDTO.getDescriptionOverlay())
                                .published(true)
                                .destinationCountryRestrictions(Set.of(UkCountry.GB, UkCountry.XI))
                                .build())
                    .collect(Collectors.toList())));
  }
}
