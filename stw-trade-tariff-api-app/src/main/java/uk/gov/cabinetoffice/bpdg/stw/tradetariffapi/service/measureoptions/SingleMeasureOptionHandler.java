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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.measureoptions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentCodeMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentaryMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ExceptionMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.NegativeMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@Component
@AllArgsConstructor
@Slf4j
public class SingleMeasureOptionHandler {

  protected static final Comparator<MeasureCondition> DOCUMENT_TYPE_COMPARATOR =
      Comparator.comparing(
          measureCondition -> measureCondition.getMeasureConditionType().getOrder());
  protected static final Comparator<MeasureCondition> DOCUMENT_CODE_COMPARATOR =
      Comparator.comparing(
          t ->
              t instanceof DocumentaryMeasureCondition
                  ? ((DocumentaryMeasureCondition) t).getDocumentCode()
                  : "");

  private final DocumentCodeDescriptionRepository documentCodeDescriptionRepository;

  public Mono<MeasureOptions> getMeasureOption(
      List<MeasureCondition> measureConditions, TradeType tradeType, Locale locale) {

    if (CollectionUtils.isEmpty(measureConditions)) {
      return Mono.empty();
    }

    List<MeasureCondition> nonNegativeMeasureConditions =
        measureConditions.stream()
            .filter(measureCondition -> !(measureCondition instanceof NegativeMeasureCondition))
            .collect(Collectors.toList());

    List<DocumentaryMeasureCondition> documentaryMeasureConditions =
        nonNegativeMeasureConditions.stream()
            .filter(DocumentaryMeasureCondition.class::isInstance)
            .map(DocumentaryMeasureCondition.class::cast)
            .collect(Collectors.toList());

    List<MeasureCondition> thresholdOptions =
        nonNegativeMeasureConditions.stream()
            .filter(ThresholdMeasureCondition.class::isInstance)
            .collect(Collectors.toList());

    List<Mono<MeasureOption>> measureOptionsList =
        convertToMeasureOptions(documentaryMeasureConditions, thresholdOptions, tradeType, locale);

    return Flux.fromIterable(measureOptionsList)
        .flatMapSequential(Function.identity())
        .collectList()
        .map(measureOptions -> MeasureOptions.builder().options(measureOptions).build());
  }

  private List<Mono<MeasureOption>> convertToMeasureOptions(
      List<DocumentaryMeasureCondition> nonThresholdConditions,
      List<MeasureCondition> thresholdConditions,
      TradeType tradeType,
      Locale locale) {
    List<DocumentaryMeasureCondition> documentaryMeasureConditions =
        new ArrayList<>(nonThresholdConditions);
    documentaryMeasureConditions.sort(
        DOCUMENT_TYPE_COMPARATOR.thenComparing(DOCUMENT_CODE_COMPARATOR));
    long totalNumberOfCertificates =
        documentaryMeasureConditions.stream()
            .filter(mc -> MeasureConditionType.CERTIFICATE.equals(mc.getMeasureConditionType()))
            .count();

    thresholdConditions.sort(Comparator.comparing(MeasureCondition::getMeasureConditionKey));

    List<MeasureCondition> measureConditions = new ArrayList<>(documentaryMeasureConditions);
    measureConditions.addAll(thresholdConditions);

    return measureConditions.stream()
        .map(
            measureCondition ->
                this.convertToMeasureOption(
                    measureCondition, totalNumberOfCertificates, tradeType, locale))
        .collect(Collectors.toList());
  }

  Mono<MeasureOption> convertToMeasureOption(
      MeasureCondition measureCondition,
      long totalNumberOfCertificates,
      TradeType tradeType,
      Locale locale) {
    switch (measureCondition.getMeasureConditionType()) {
      case THRESHOLD:
        try {
          return Mono.just(
              ThresholdMeasureOption.builder()
                  .threshold((ThresholdMeasureCondition) measureCondition)
                  .locale(locale)
                  .build());
        } catch (IllegalArgumentException illegalArgumentException) {
          log.warn("Threshold measure condition could not be built", illegalArgumentException);
        }
        return Mono.empty();
      case CERTIFICATE:
        return getDocumentCodeDescription(
                (DocumentaryMeasureCondition) measureCondition, tradeType, locale)
            .map(
                dcd ->
                    DocumentCodeMeasureOption.builder()
                        .totalNumberOfCertificates(totalNumberOfCertificates)
                        .documentCodeDescription(dcd)
                        .build());
      case EXCEPTION:
        return getDocumentCodeDescription(
                (DocumentaryMeasureCondition) measureCondition, tradeType, locale)
            .map(
                dcd ->
                    ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                        .documentCodeDescription(dcd)
                        .build());
      default:
        return Mono.empty();
    }
  }

  protected Mono<List<DocumentCodeDescription>> getDocumentCodeDescriptionsByMeasureConditions(
      List<DocumentCodeDescription> listOfDocumentCodeDescriptions,
      List<DocumentaryMeasureCondition> measureConditions) {
    return Mono.just(
        measureConditions.stream()
            .map(
                mc ->
                    listOfDocumentCodeDescriptions.stream()
                        .filter(dcd -> dcd.getDocumentCode().equals(mc.getDocumentCode()))
                        .findFirst()
                        .orElseGet(
                            () ->
                                DocumentCodeDescription.builder()
                                    .documentCode(mc.getDocumentCode())
                                    .descriptionOverlay(mc.getDescription())
                                    .build()))
            .collect(Collectors.toList()));
  }

  protected Mono<List<DocumentCodeDescription>> getDocumentCodeDescriptions(
      List<DocumentaryMeasureCondition> measureConditions, TradeType tradeType, Locale locale) {
    return documentCodeDescriptionRepository
        .findDocumentCodeDescriptionsByDocumentCodesAndTradeTypeAndLocale(
            measureConditions.stream()
                .map(DocumentaryMeasureCondition::getDocumentCode)
                .collect(Collectors.toList()),
            tradeType,
            locale)
        .collectList()
        .flatMap(dcd -> getDocumentCodeDescriptionsByMeasureConditions(dcd, measureConditions))
        .defaultIfEmpty(
            measureConditions.stream()
                .map(
                    mc ->
                        DocumentCodeDescription.builder()
                            .documentCode(mc.getDocumentCode())
                            .descriptionOverlay(mc.getDescription())
                            .build())
                .collect(Collectors.toList()));
  }

  protected Mono<DocumentCodeDescription> getDocumentCodeDescription(
      DocumentaryMeasureCondition measureCondition, TradeType tradeType, Locale locale) {
    return getDocumentCodeDescriptions(List.of(measureCondition), tradeType, locale)
        .map(descriptionsList -> descriptionsList.get(0));
  }
}
