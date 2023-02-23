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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.AppConfig;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.MeasureTypeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.MeasureTypeDescriptionContentRepo;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Duty;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Quota;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tariff;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tax;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class DutyMeasureService {

  private final MeasureTypeDescriptionContentRepo measureTypeDescriptionContentRepo;

  public Flux<Duty> getTariffsAndTaxesMeasures(
      final List<Measure> measures, final UkCountry destinationUkCountry) {
    return Flux.fromIterable(measures)
        .filter(measure -> measure.getDutyValue().isPresent())
        .flatMap(
            measure -> {
              var locale = AppConfig.LOCALE;
              return measureTypeDescriptionContentRepo
                  .findByMeasureTypeIdInAndLocaleAndPublished(
                      List.of(measure.getMeasureType().getId()), locale, true)
                  .filter(
                      measureTypeDescription ->
                          measureTypeDescription
                              .getDestinationCountryRestrictions()
                              .contains(destinationUkCountry))
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
                                    "More than one measure type descriptions configured for measure type %s, locale %s, destination country %s",
                                    measure.getMeasureType().getId(),
                                    locale,
                                    destinationUkCountry)));
                      })
                  .defaultIfEmpty(
                      MeasureTypeDescription.builder()
                          .measureTypeId(measure.getMeasureType().getId())
                          .descriptionOverlay(measure.getMeasureType().getDescription())
                          .build())
                  .zipWith(Mono.defer(() -> Mono.just(measure)));
            })
        .map(
            measureTypeDescriptionWithOptions ->
                measureTypeDescriptionWithOptions.getT2().isTaxMeasure()
                    ? Tax.builder()
                        .measureTypeId(measureTypeDescriptionWithOptions.getT1().getMeasureTypeId())
                        .text(measureTypeDescriptionWithOptions.getT1().getDescriptionOverlay())
                        .additionalCode(
                            measureTypeDescriptionWithOptions
                                .getT2()
                                .getAdditionalCode()
                                .orElse(null))
                        .value(
                            measureTypeDescriptionWithOptions.getT2().getDutyValue().orElse(null))
                        .geographicalArea(
                            measureTypeDescriptionWithOptions.getT2().getGeographicalArea())
                        .build()
                    : Tariff.builder()
                        .measureTypeId(measureTypeDescriptionWithOptions.getT1().getMeasureTypeId())
                        .text(measureTypeDescriptionWithOptions.getT1().getDescriptionOverlay())
                        .additionalCode(
                            measureTypeDescriptionWithOptions
                                .getT2()
                                .getAdditionalCode()
                                .orElse(null))
                        .value(
                            measureTypeDescriptionWithOptions.getT2().getDutyValue().orElse(null))
                        .quota(
                            measureTypeDescriptionWithOptions
                                .getT2()
                                .getQuotaNumber()
                                .map(quotaNumber -> Quota.builder().number(quotaNumber).build())
                                .orElse(null))
                        .geographicalArea(
                            measureTypeDescriptionWithOptions.getT2().getGeographicalArea())
                        .build());
  }
}
