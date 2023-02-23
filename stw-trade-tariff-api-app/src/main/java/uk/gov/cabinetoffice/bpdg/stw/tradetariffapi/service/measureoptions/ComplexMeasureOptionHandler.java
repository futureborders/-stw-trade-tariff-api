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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.AppConfig;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionContentRepo;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ExceptionAndThresholdMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MultiCertificateMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ThresholdMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;

@Component
@Slf4j
public class ComplexMeasureOptionHandler extends SingleMeasureOptionHandler {

  public ComplexMeasureOptionHandler(
      DocumentCodeDescriptionContentRepo documentCodeDescriptionContentRepo) {
    super(documentCodeDescriptionContentRepo);
  }

  @Override
  public Mono<MeasureOptions> getMeasureOption(
      List<MeasureCondition> measureConditions, UkCountry destinationUkCountry) {

    if (CollectionUtils.isEmpty(measureConditions)) {
      return Mono.empty();
    }

    List<MeasureCondition> disjointCertificates = new ArrayList<>();
    MeasureCondition disjointThreshold = null;
    List<MeasureCondition> commonMeasureConditions = new ArrayList<>();
    Set<String> measureConditionKeys = new HashSet<>();
    for (int measureIndex = 0; measureIndex < measureConditions.size(); measureIndex++) {
      var measureCondition = measureConditions.get(measureIndex);
      if (measureCondition.getMeasureConditionType() == MeasureConditionType.NEGATIVE
          || measureConditionKeys.contains(measureCondition.getMeasureConditionKey())) {
        continue;
      }
      measureConditionKeys.add(measureCondition.getMeasureConditionKey());
      if (isMeasureConditionPresentInExistingMeasureConditions(
          measureCondition, measureConditions, measureIndex)) {
        commonMeasureConditions.add(measureCondition);
      } else {
        if (StringUtils.hasLength(measureCondition.getDocumentCode())) {
          disjointCertificates.add(measureCondition);
        } else {
          disjointThreshold = measureCondition;
        }
      }
    }

    List<Mono<MeasureOption>> measureOptionsList =
        convertToMeasureOptions(
            commonMeasureConditions, disjointCertificates, disjointThreshold, destinationUkCountry);

    return Flux.fromIterable(measureOptionsList)
        .flatMapSequential(Function.identity())
        .collectList()
        .map(measureOptions -> MeasureOptions.builder().options(measureOptions).build());
  }

  private boolean isMeasureConditionPresentInExistingMeasureConditions(
      MeasureCondition measureConditionToCheck,
      List<MeasureCondition> measureConditions,
      int measureConditionIndex) {
    if (measureConditionIndex == measureConditions.size() - 1) {
      return false;
    }
    for (int j = measureConditionIndex + 1; j < measureConditions.size(); j++) {
      if (measureConditionToCheck
          .getMeasureConditionKey()
          .equals(measureConditions.get(j).getMeasureConditionKey())) {
        return true;
      }
    }
    return false;
  }

  private List<Mono<MeasureOption>> convertToMeasureOptions(
      List<MeasureCondition> commonMeasureConditions,
      List<MeasureCondition> disjointCertificates,
      MeasureCondition disjointThreshold,
      UkCountry destinationUkCountry) {
    List<Mono<MeasureOption>> measureOptionList = new ArrayList<>();

    List<MeasureCondition> commonMeasureConditionsSortedList =
        new ArrayList<>(commonMeasureConditions);
    commonMeasureConditionsSortedList.sort(DOCUMENT_TYPE_COMPARATOR.thenComparing(DOCUMENT_CODE_COMPARATOR));
    long totalNumberOfCertificates =
        commonMeasureConditionsSortedList.stream()
            .filter(mc -> MeasureConditionType.CERTIFICATE.equals(mc.getMeasureConditionType()))
            .count();

    for (MeasureCondition measureCondition : commonMeasureConditionsSortedList) {
      measureOptionList.add(
          this.convertToMeasureOption(
              measureCondition, totalNumberOfCertificates, destinationUkCountry));
    }
    if (!CollectionUtils.isEmpty(disjointCertificates)) {
      if (disjointCertificates.size() > 1) {
        disjointCertificates.sort(DOCUMENT_CODE_COMPARATOR);
        Mono<MeasureOption> multiCertificateMeasureOption =
            getDocumentCodeDescriptions(
                    disjointCertificates, destinationUkCountry, AppConfig.LOCALE)
                .map(
                    documentCodeDescriptions ->
                        MultiCertificateMeasureOption.builder()
                            .certificate1(documentCodeDescriptions.get(0))
                            .certificate2(documentCodeDescriptions.get(1))
                            .build());
        measureOptionList.add(multiCertificateMeasureOption);
      } else {
        measureOptionList.add(
            maybeBuildThresholdCertificateMeasureOption(
                disjointCertificates.get(0), disjointThreshold, destinationUkCountry));
      }
    }
    return measureOptionList;
  }

  private Mono<MeasureOption> maybeBuildThresholdCertificateMeasureOption(
      MeasureCondition measureConditionForDisjointCertificate,
      MeasureCondition measureConditionForDisjointThreshold,
      UkCountry destinationUkCountry) {
    if (measureConditionForDisjointCertificate != null
        && measureConditionForDisjointThreshold != null) {
      try {
        ThresholdMeasureOption thresholdMeasureOption = ThresholdMeasureOption.builder().threshold(measureConditionForDisjointThreshold).build();
        var locale = AppConfig.LOCALE;
        return getDocumentCodeDescription(
            measureConditionForDisjointCertificate, destinationUkCountry, locale)
            .map(
                documentCodeDescription ->
                    ExceptionAndThresholdMeasureOption.exceptionAndThresholdBuilder()
                        .exception(documentCodeDescription)
                        .thresholdMeasure(thresholdMeasureOption)
                        .build());
      } catch (IllegalArgumentException illegalArgumentException){
        log.warn("Threshold measure condition could not be built", illegalArgumentException);
      }
    }
    return Mono.empty();
  }
}
