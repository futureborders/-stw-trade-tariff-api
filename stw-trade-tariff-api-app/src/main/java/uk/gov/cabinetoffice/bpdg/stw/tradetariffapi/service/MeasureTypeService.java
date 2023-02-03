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

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.MeasureTypeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.MeasureTypeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ConditionBasedRestrictiveMeasure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.RestrictiveMeasure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class MeasureTypeService {

  private final MeasureTypeDescriptionRepository measureTypeDescriptionRepository;
  private final MeasureOptionService measureOptionService;

  private final Map<String, String> measureOverrideText =
      Map.of(
          "362",
          "%0A%0AIf you import precursor chemicals, you need authorisation from the Home Office. [Read about precursor chemical licensing](https://www.gov.uk/guidance/precursor-chemical-licensing)");

  private final Map<String, Tuple2<String, String>> commoditySpecificMeasureOverrideText =
      Map.of(
          "2309902000",
          Tuples.of(
              "465",
              "%0A%0AIf you do not have a [certificate issued by the Federal Grain Inspection Service](https://www.legislation.gov.uk/eur/2007/1375/annex/I) and a [certificate issued by the USA wet milling industry](https://www.legislation.gov.uk/eur/2017/337/images/eur_20170337_2017-02-27_en_001?view=extent), your goods must go for laboratory analysis in the UK.%0A%0AIf you import residues from the manufacture of starch from maize from the USA, your goods will be subject to random sampling at the UK border if they’re accompanied by a [certificate issued by the Federal Grain Inspection Service](https://www.legislation.gov.uk/eur/2007/1375/annex/I)%0A%0AIn this case, you will need a [certificate issued by the USA wet milling industry](https://www.legislation.gov.uk/eur/2017/337/images/eur_20170337_2017-02-27_en_001?view=extent) when you import these goods."));

  public Flux<RestrictiveMeasure> getRestrictiveMeasures(
      List<Measure> measures, String commodityCode, TradeType tradeType, Locale locale) {
    return Flux.fromIterable(measures)
        .flatMap(
            measure ->
                Mono.zip(
                    this.measureTypeDescriptionRepository
                        .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                            List.of(measure.getMeasureType().getId()), tradeType, locale)
                        .collectList()
                        .flatMap(
                            listOfMeasureTypeDescriptions -> {
                              if (CollectionUtils.isEmpty(listOfMeasureTypeDescriptions)) {
                                return Mono.empty();
                              } else if (listOfMeasureTypeDescriptions.size() == 1) {
                                return Mono.just(listOfMeasureTypeDescriptions.get(0));
                              }
                              return Mono.error(
                                  new RuntimeException(
                                      format(
                                          "More than one measure type descriptions configured for measure type %s, locale %s",
                                          measure.getMeasureType().getId(), locale)));
                            })
                        .defaultIfEmpty(
                            MeasureTypeDescription.builder()
                                .measureTypeId(measure.getMeasureType().getId())
                                .descriptionOverlay(measure.getMeasureType().getDescription())
                                .build()),
                    this.measureOptionService
                        .getMeasureOptions(measure.getMeasureConditions(), tradeType, locale)
                        .collectList(),
                    Mono.just(measure)))
        .filter(
            measureTypeDescriptionWithOptions ->
                !measureTypeDescriptionWithOptions.getT2().isEmpty())
        .map(
            measureTypeDescriptionWithOptions -> {
              if (commoditySpecificMeasureOverrideText.containsKey(commodityCode)
                  && commoditySpecificMeasureOverrideText
                      .get(commodityCode)
                      .getT1()
                      .equals(measureTypeDescriptionWithOptions.getT1().getMeasureTypeId())) {
                String descriptionOverlay =
                    measureTypeDescriptionWithOptions.getT1().getDescriptionOverlay()
                        + commoditySpecificMeasureOverrideText.get(commodityCode).getT2();
                return ConditionBasedRestrictiveMeasure.builder()
                    .id(measureTypeDescriptionWithOptions.getT1().getMeasureTypeId())
                    .descriptionOverlay(descriptionOverlay)
                    .description(descriptionOverlay)
                    .measureTypeSeries(
                        measureTypeDescriptionWithOptions.getT3().getMeasureType().getSeriesId())
                    .build();
              }
              if (measureOverrideText.containsKey(
                  measureTypeDescriptionWithOptions.getT1().getMeasureTypeId())) {
                String descriptionOverlay =
                    measureTypeDescriptionWithOptions.getT1().getDescriptionOverlay()
                        + measureOverrideText.get(
                            measureTypeDescriptionWithOptions.getT1().getMeasureTypeId());
                return ConditionBasedRestrictiveMeasure.builder()
                    .id(measureTypeDescriptionWithOptions.getT1().getMeasureTypeId())
                    .descriptionOverlay(descriptionOverlay)
                    .description(descriptionOverlay)
                    .measureTypeSeries(
                        measureTypeDescriptionWithOptions.getT3().getMeasureType().getSeriesId())
                    .build();
              }
              return ConditionBasedRestrictiveMeasure.builder()
                  .id(measureTypeDescriptionWithOptions.getT1().getMeasureTypeId())
                  .measureTypeSeries(
                      measureTypeDescriptionWithOptions.getT3().getMeasureType().getSeriesId())
                  .descriptionOverlay(
                      measureTypeDescriptionWithOptions.getT1().getDescriptionOverlay())
                  .description(measureTypeDescriptionWithOptions.getT1().getDescriptionOverlay())
                  .measureOptions(measureTypeDescriptionWithOptions.getT2())
                  .build();
            });
  }
}
