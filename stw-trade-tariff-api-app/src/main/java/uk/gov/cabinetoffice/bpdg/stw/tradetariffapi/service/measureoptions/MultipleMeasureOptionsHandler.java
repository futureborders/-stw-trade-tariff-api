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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@AllArgsConstructor
@Component
public class MultipleMeasureOptionsHandler {

  private final SingleMeasureOptionHandler singleMeasureOptionHandler;

  public Flux<MeasureOptions> getMeasureOptions(
      List<MeasureCondition> measureConditions, TradeType tradeType, Locale locale) {

    if (CollectionUtils.isEmpty(measureConditions)) {
      return Flux.empty();
    }

    Map<MeasureConditionCode, List<MeasureCondition>> measureConditionsByConditionCode =
        measureConditions.stream()
            .collect(Collectors.groupingBy(MeasureCondition::getConditionCode));

    List<Mono<MeasureOptions>> measuresList =
        measureConditionsByConditionCode.keySet().stream()
            .sorted()
            .map(measureConditionsByConditionCode::get)
            .map(
                measureConditionsByCode ->
                    singleMeasureOptionHandler.getMeasureOption(measureConditionsByCode, tradeType, locale))
            .collect(Collectors.toList());

    return Flux.fromIterable(measuresList).flatMapSequential(Function.identity());
  }
}
