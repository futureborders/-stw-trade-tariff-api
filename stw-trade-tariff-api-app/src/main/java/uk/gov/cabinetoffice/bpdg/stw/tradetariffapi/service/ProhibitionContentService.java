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

import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.MeasureFilterConstantsConfig.MEASURE_TYPE_SERIES_ID_A;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.MeasureFilterConstantsConfig.MEASURE_TYPE_SERIES_ID_B;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.ProhibitionDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.ProhibitionContentRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Prohibition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ProhibitionContentService {

  private final ProhibitionContentRepository prohibitionContentRepository;
  private Mono<List<ProhibitionDescription>> prohibitionDescriptions;

  @PostConstruct
  public void initializeProhibitionContent() {
    prohibitionDescriptions =
      prohibitionContentRepository.findAll().collectList().subscribeOn(Schedulers.single());
  }

  public Mono<List<Prohibition>> getProhibitions(
    List<Measure> restrictedMeasures, final String originCountry, final Locale locale, TradeType tradeType) {
    Mono<Tuple2<List<Measure>, List<ProhibitionDescription>>>
      measureAndProhibitionDescriptionTuple2Mono =
      Mono.zip(
        Mono.just(
          restrictedMeasures.stream()
            .filter(this::filterProhibitiveMeasures)
            .collect(Collectors.toList())),
        prohibitionDescriptions);

    return measureAndProhibitionDescriptionTuple2Mono.flatMap(
      measureAndProhibitionDescriptionTuple2 -> {
        var prohibitedMeasures = measureAndProhibitionDescriptionTuple2.getT1();
        var prohibitionDescriptions = measureAndProhibitionDescriptionTuple2.getT2();
        return Mono.just(
          prohibitedMeasures.stream()
            .map(
              measure ->
                Tuples.of(
                  measure,
                  this.getProhibitionDescription(
                    measure, prohibitionDescriptions, originCountry, locale, tradeType)))
            .map(this::prohibition)
            .collect(Collectors.toList()));
      });
  }

  private Prohibition prohibition(
    Tuple2<Measure, ProhibitionDescription> measureWithProhibitionDescriptionTuple2) {
    var measure = measureWithProhibitionDescriptionTuple2.getT1();
    var prohibitionDescription = measureWithProhibitionDescriptionTuple2.getT2();
    return Prohibition.builder()
      .legalAct(prohibitionDescription.getLegalAct())
      .description(prohibitionDescription.getDescription())
      .measureTypeSeries(measure.getMeasureType().getSeriesId())
      .measureTypeId(measure.getMeasureType().getId())
      .id(measure.getMeasureType().getId())
      .build();
  }

  private boolean filterProhibitiveMeasures(Measure measure) {
    return MEASURE_TYPE_SERIES_ID_A.equals(measure.getMeasureType().getSeriesId())
      || (MEASURE_TYPE_SERIES_ID_B.equals(measure.getMeasureType().getSeriesId())
      && (measure.getAdditionalCode().isEmpty()
      || !measure.getAdditionalCode().get().isResidual())
      && measure.getMeasureConditions().isEmpty());
  }

  private ProhibitionDescription getProhibitionDescription(
    Measure measure,
    List<ProhibitionDescription> prohibitionDescriptions,
    String originCountry,
    Locale locale,
    TradeType tradeType) {
    return StringUtils.isBlank(measure.getLegalActId())
      ? defaultProhibitionDescription(originCountry, null)
      : prohibitionDescription(
        prohibitionDescriptions, originCountry, locale, measure.getLegalActId(), tradeType);
  }

  private ProhibitionDescription prohibitionDescription(
    List<ProhibitionDescription> prohibitionDescriptions,
    String originCountry,
    Locale locale,
    String legalAct,
    TradeType tradeType) {
    List<ProhibitionDescription> prohibitionDescriptionsByTradeType =
        prohibitionDescriptions.stream()
            .filter(prohibitionDescription -> prohibitionDescription.getApplicableTradeTypes().contains(tradeType))
            .collect(Collectors.toList());
    return prohibitionDescriptionsByTradeType.stream()
      .filter(byOriginCountryAndLocaleAndLegalAct(originCountry, locale, legalAct))
      .findFirst()
      .orElse(
          prohibitionDescriptionsByTradeType.stream()
          .filter(byLocaleAndLegalAct(locale, legalAct))
          .findFirst()
          .orElse(defaultProhibitionDescription(originCountry, legalAct)));
  }

  private ProhibitionDescription defaultProhibitionDescription(
    String originCountry, String legalAct) {
    return ProhibitionDescription.builder().legalAct(legalAct).originCountry(originCountry).build();
  }

  private Predicate<ProhibitionDescription> byLocaleAndLegalAct(Locale locale, String legalAct) {
    return (ProhibitionDescription prohibitionDescription) ->
      prohibitionDescription.getLegalAct().contains(legalAct)
        && prohibitionDescription.getLocale().equals(locale);
  }

  private Predicate<ProhibitionDescription> byOriginCountryAndLocaleAndLegalAct(
    String originCountry, Locale locale, String legalAct) {
    return (ProhibitionDescription prohibitionDescription) ->
      prohibitionDescription.getLegalAct().contains(legalAct)
        && originCountry.equals(prohibitionDescription.getOriginCountry())
        && prohibitionDescription.getLocale().equals(locale);
  }
}
