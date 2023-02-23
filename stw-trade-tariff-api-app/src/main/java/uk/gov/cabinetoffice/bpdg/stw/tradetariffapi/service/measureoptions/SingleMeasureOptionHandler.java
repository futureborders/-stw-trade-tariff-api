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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.AppConfig;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionContentRepo;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentCodeMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ExceptionMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;

@Component
@AllArgsConstructor
@Slf4j
public class SingleMeasureOptionHandler {

  protected static final Comparator<MeasureCondition> DOCUMENT_TYPE_COMPARATOR =
      Comparator.comparing(
          measureCondition -> measureCondition.getMeasureConditionType().getOrder());
  protected static final Comparator<MeasureCondition> DOCUMENT_CODE_COMPARATOR =
      Comparator.comparing(MeasureCondition::getDocumentCode);

  private final DocumentCodeDescriptionContentRepo documentCodeDescriptionContentRepo;

  public Mono<MeasureOptions> getMeasureOption(
      List<MeasureCondition> measureConditions, UkCountry destinationUkCountry) {

    if (CollectionUtils.isEmpty(measureConditions)) {
      return Mono.empty();
    }

    List<MeasureCondition> nonThresholdOptions = new ArrayList<>();
    MeasureCondition thresholdOption = null;
    for (MeasureCondition measureCondition : measureConditions) {
      if (measureCondition.getMeasureConditionType() == MeasureConditionType.NEGATIVE) {
        continue;
      }
      if (StringUtils.hasLength(measureCondition.getDocumentCode())) {
        nonThresholdOptions.add(measureCondition);
      } else {
        thresholdOption = measureCondition;
      }
    }

    List<Mono<MeasureOption>> measureOptionsList =
        convertToMeasureOptions(nonThresholdOptions, thresholdOption, destinationUkCountry);

    return Flux.fromIterable(measureOptionsList)
        .flatMapSequential(Function.identity())
        .collectList()
        .map(measureOptions -> MeasureOptions.builder().options(measureOptions).build());
  }

  private List<Mono<MeasureOption>> convertToMeasureOptions(
      List<MeasureCondition> nonThresholdConditions,
      MeasureCondition thresholdCondition,
      UkCountry destinationUkCountry) {
    List<MeasureCondition> measureConditions = new ArrayList<>(nonThresholdConditions);
    measureConditions.sort(DOCUMENT_TYPE_COMPARATOR.thenComparing(DOCUMENT_CODE_COMPARATOR));
    long totalNumberOfCertificates =
        measureConditions.stream()
            .filter(mc -> MeasureConditionType.CERTIFICATE.equals(mc.getMeasureConditionType()))
            .count();

    Optional.ofNullable(thresholdCondition).ifPresent(measureConditions::add);

    return measureConditions.stream()
        .map(
            measureCondition ->
                this.convertToMeasureOption(
                    measureCondition, totalNumberOfCertificates, destinationUkCountry))
        .collect(Collectors.toList());
  }

  Mono<MeasureOption> convertToMeasureOption(
      MeasureCondition measureCondition,
      long totalNumberOfCertificates,
      UkCountry destinationUkCountry) {
    var locale = AppConfig.LOCALE;
    switch (measureCondition.getMeasureConditionType()) {
      case THRESHOLD:
        try {
          return Mono.just(ThresholdMeasureOption.builder().threshold(measureCondition).build());
        } catch (IllegalArgumentException illegalArgumentException){
          log.warn("Threshold measure condition could not be built", illegalArgumentException);
        }
        return Mono.empty();
      case CERTIFICATE:
        return getDocumentCodeDescription(measureCondition, destinationUkCountry, locale)
            .map(
                dcd ->
                    DocumentCodeMeasureOption.builder()
                        .totalNumberOfCertificates(totalNumberOfCertificates)
                        .documentCodeDescription(dcd)
                        .build());
      case EXCEPTION:
        return getDocumentCodeDescription(measureCondition, destinationUkCountry, locale)
            .map(
                dcd ->
                    ExceptionMeasureOption.exceptionMeasureOptionBuilder()
                        .documentCodeDescription(dcd)
                        .build());
    }
    return Mono.empty();
  }

  protected Mono<List<DocumentCodeDescription>> getDocumentCodeDescriptionsByMeasureConditions(
      List<DocumentCodeDescription> listOfDocumentCodeDescriptions,
      List<MeasureCondition> measureConditions) {
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
                                        .descriptionOverlay(mc.getRequirement())
                                        .build()))
                .collect(Collectors.toList()));
  }

  protected Mono<List<DocumentCodeDescription>> getDocumentCodeDescriptions(
      List<MeasureCondition> measureConditions, UkCountry destinationUkCountry, Locale locale) {
    return documentCodeDescriptionContentRepo
        .findByDocumentCodeInAndLocaleAndPublished(
            measureConditions.stream()
                .map(MeasureCondition::getDocumentCode)
                .collect(Collectors.toList()),
            locale, true)
        .filter(dcd -> dcd.getDestinationCountryRestrictions().contains(destinationUkCountry))
        .collectList()
        .flatMap(dcd -> getDocumentCodeDescriptionsByMeasureConditions(dcd, measureConditions))
        .defaultIfEmpty(
            measureConditions.stream()
                .map(
                    mc ->
                        DocumentCodeDescription.builder()
                            .documentCode(mc.getDocumentCode())
                            .descriptionOverlay(mc.getRequirement())
                            .build())
                .collect(Collectors.toList()));
  }

  protected Mono<DocumentCodeDescription> getDocumentCodeDescription(
      MeasureCondition measureCondition, UkCountry destinationUkCountry, Locale locale) {
    return getDocumentCodeDescriptions(List.of(measureCondition), destinationUkCountry, locale)
        .map(descriptionsList -> descriptionsList.get(0));
  }
}
