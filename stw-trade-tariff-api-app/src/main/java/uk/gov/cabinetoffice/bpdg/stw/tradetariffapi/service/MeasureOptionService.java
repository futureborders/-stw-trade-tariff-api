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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentaryMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.NegativeMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.measureoptions.ComplexMeasureOptionHandler;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.measureoptions.MultipleMeasureOptionsHandler;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.measureoptions.SingleMeasureOptionHandler;

@Service
@Slf4j
public class MeasureOptionService {

  private static final String CDS_UNIVERSAL_WAIVER = "999L";

  private final MultipleMeasureOptionsHandler multipleMeasureOptionsHandler;
  private final SingleMeasureOptionHandler singleMeasureOptionHandler;
  private final ComplexMeasureOptionHandler complexMeasureHandler;

  @Autowired
  public MeasureOptionService(
      MultipleMeasureOptionsHandler multipleMeasureOptionsHandler,
      SingleMeasureOptionHandler singleMeasureOptionHandler,
      ComplexMeasureOptionHandler complexMeasureHandler) {
    this.multipleMeasureOptionsHandler = multipleMeasureOptionsHandler;
    this.singleMeasureOptionHandler = singleMeasureOptionHandler;
    this.complexMeasureHandler = complexMeasureHandler;
  }

  public Flux<MeasureOptions> getMeasureOptions(
      List<MeasureCondition> measureConditions, TradeType tradeType, Locale locale) {

    List<MeasureCondition> positiveMeasureConditions =
        measureConditions.stream()
            .filter(measureCondition -> !(measureCondition instanceof NegativeMeasureCondition))
            .collect(Collectors.toList());

    Set<MeasureConditionCode> conditionCodes =
        positiveMeasureConditions.stream()
            .map(MeasureCondition::getConditionCode)
            .collect(Collectors.toSet());

    return conditionCodes.size() <= 1
        ? Flux.from(
            singleMeasureOptionHandler.getMeasureOption(
                positiveMeasureConditions, tradeType, locale))
        : isAComplexMeasure(positiveMeasureConditions)
            ? Flux.from(
                complexMeasureHandler.getMeasureOption(
                    positiveMeasureConditions, tradeType, locale))
            : multipleMeasureOptionsHandler.getMeasureOptions(
                positiveMeasureConditions, tradeType, locale);
  }

  private boolean isAComplexMeasure(List<MeasureCondition> measureConditions) {
    Map<String, List<DocumentaryMeasureCondition>> documentaryMeasureConditionsByDocumentCode =
        measureConditions.stream()
            .filter(DocumentaryMeasureCondition.class::isInstance)
            .map(DocumentaryMeasureCondition.class::cast)
            // don't treat CDS universal waiver as complex as they are not set up correctly
            .filter(
                measureCondition ->
                    !CDS_UNIVERSAL_WAIVER.equals(measureCondition.getDocumentCode()))
            .collect(Collectors.groupingBy(DocumentaryMeasureCondition::getDocumentCode));

    return documentaryMeasureConditionsByDocumentCode.values().stream()
        .anyMatch(documentaryMeasureConditions -> documentaryMeasureConditions.size() > 1);
  }
}
