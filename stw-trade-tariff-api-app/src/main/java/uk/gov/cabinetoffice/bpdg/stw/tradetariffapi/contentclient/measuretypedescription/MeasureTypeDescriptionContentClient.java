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
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.MeasureTypeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@Component
@AllArgsConstructor
@Slf4j
public class MeasureTypeDescriptionContentClient implements MeasureTypeDescriptionRepository {
  private static final String MEASURE_TYPE_DESCRIPTIONS_API_ENDPOINT =
      "/api/v1/measure-type-descriptions";
  private static final String MEASURE_TYPES = "measureTypes";
  private static final String LOCALE = "locale";
  private static final String TRADE_TYPE = "tradeType";
  private final WebClient webClient;
  private final ApplicationProperties applicationProperties;

  public Flux<MeasureTypeDescription> findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
      List<String> measureTypes, TradeType tradeType, Locale locale) {
    var contentApiConfiguration = applicationProperties.getContentApi();
    log.debug(
        "Calling the content api with baseUrl: {}, api path: {}, measureTypes: {}, tradeType: {} and locale: {}",
        contentApiConfiguration.getUrl(),
        MEASURE_TYPE_DESCRIPTIONS_API_ENDPOINT,
        measureTypes,
        tradeType,
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
                        .queryParam(TRADE_TYPE, tradeType.name())
                        .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(MeasureTypeDescriptionResponseDTO.class)
            .onErrorResume(
                ex -> {
                  log.error(
                      "Error occurred while fetching measure type descriptions for measure types : {} by tradeType: {} and locale : {}",
                      measureTypes,
                      tradeType,
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
                                .build())
                    .collect(Collectors.toList())));
  }
}
