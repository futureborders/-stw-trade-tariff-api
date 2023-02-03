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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@Service
public class MeasureFilterer {

  private static final List<String> RESTRICTIVE_MEASURE_TYPE_ID_EXCLUSION_LIST =
      List.of("464", "481", "482", "483", "484", "495", "496", "730");
  private static final List<String> RESTRICTIVE_MEASURE_TYPE_SERIES_LIST =
      List.of(MEASURE_TYPE_SERIES_ID_A, MEASURE_TYPE_SERIES_ID_B);

  private static final List<String> TAX_AND_DUTY_MEASURE_TYPE_SERIES_LIST =
      List.of("C", "D", "J", "P", "Q");

  public List<Measure> getRestrictiveMeasures(
      List<Measure> measures, TradeType tradeType, String tradeDestinationCountry) {
    List<Measure> filteredMeasuresByTradeTypeAndAllowedSeriesIdAndDisallowedMeasureId =
        measures.stream()
            .filter(
                measureResponse -> measureResponse.getApplicableTradeTypes().contains(tradeType))
            .filter(
                measure ->
                    RESTRICTIVE_MEASURE_TYPE_SERIES_LIST.contains(
                        measure.getMeasureType().getSeriesId()))
            .filter(
                measure ->
                    !RESTRICTIVE_MEASURE_TYPE_ID_EXCLUSION_LIST.contains(
                        measure.getMeasureType().getId()))
            .collect(Collectors.toList());

    String country = convertToEUIfNeeded(tradeDestinationCountry);

    List<Measure> filteredMeasuresByTradeTypeAndAssignedToSameCountry =
        filteredMeasuresByTradeTypeAndAllowedSeriesIdAndDisallowedMeasureId.stream()
            .filter(
                measureResponse -> measureResponse.getGeographicalArea().getId().equals(country))
            .collect(Collectors.toList());

    Set<MeasureType> measureTypeResponsesAssignedToSameCountry =
        filteredMeasuresByTradeTypeAndAssignedToSameCountry.stream()
            .map(Measure::getMeasureType)
            .collect(Collectors.toSet());

    List<Measure> filteredMeasuresByTradeTypeAndAssignedToGlobalAreas =
        filteredMeasuresByTradeTypeAndAllowedSeriesIdAndDisallowedMeasureId.stream()
            .filter(
                measureResponse ->
                    Optional.ofNullable(
                            measureResponse.getGeographicalArea().getChildrenGeographicalAreas())
                        .orElse(Set.of())
                        .contains(country))
            .filter(measureResponse -> !measureResponse.getExcludedCountries().contains(country))
            .filter(
                measureResponse ->
                    !measureTypeResponsesAssignedToSameCountry.contains(
                        measureResponse.getMeasureType()))
            .collect(Collectors.toList());

    return Stream.concat(
            filteredMeasuresByTradeTypeAndAssignedToGlobalAreas.stream(),
            filteredMeasuresByTradeTypeAndAssignedToSameCountry.stream())
        .collect(Collectors.toList());
  }

  public List<Measure> getTaxAndDutyMeasures(
      List<Measure> measures, TradeType tradeType, String tradeDestinationCountry) {

    List<Measure> filteredMeasuresByTradeTypeAndAllowedSeriesIdAndDisallowedMeasureId =
        measures.stream()
            .filter(
                measureResponse -> measureResponse.getApplicableTradeTypes().contains(tradeType))
            .filter(
                measure ->
                    TAX_AND_DUTY_MEASURE_TYPE_SERIES_LIST.contains(
                        measure.getMeasureType().getSeriesId()))
            .collect(Collectors.toList());

    String country = convertToEUIfNeeded(tradeDestinationCountry);

    List<Measure> filteredMeasuresByTradeTypeAndAssignedToCountry =
        filteredMeasuresByTradeTypeAndAllowedSeriesIdAndDisallowedMeasureId.stream()
            .filter(
                measureResponse -> measureResponse.getGeographicalArea().getId().equals(country))
            .collect(Collectors.toList());

    List<Measure> filteredMeasuresByTradeTypeAndAssignedToGlobalAreas =
        filteredMeasuresByTradeTypeAndAllowedSeriesIdAndDisallowedMeasureId.stream()
            .filter(
                measureResponse ->
                    Optional.ofNullable(
                            measureResponse.getGeographicalArea().getChildrenGeographicalAreas())
                        .orElse(Set.of())
                        .contains(country))
            .filter(measureResponse -> !measureResponse.getExcludedCountries().contains(country))
            .collect(Collectors.toList());

    return Stream.concat(
            filteredMeasuresByTradeTypeAndAssignedToGlobalAreas.stream(),
            filteredMeasuresByTradeTypeAndAssignedToCountry.stream())
        .collect(Collectors.toList());
  }

  public List<Measure> maybeFilterByAdditionalCode(
      List<Measure> allFilteredMeasures, Optional<String> additionalCode) {
    Optional<Measure> maybeAdditionalCodeMeasure =
        additionalCode.flatMap(
            ac ->
                allFilteredMeasures.stream()
                    .filter(
                        m ->
                            m.getAdditionalCode().isPresent()
                                && m.getAdditionalCode().get().getCode().equals(ac))
                    .findFirst());

    return maybeAdditionalCodeMeasure
        .flatMap(Measure::getAdditionalCode)
        .map(
            ac -> {
              if (ac.isResidual()) {
                return allFilteredMeasures.stream()
                    .filter(m -> m.getAdditionalCode().isEmpty())
                    .collect(Collectors.toList());
              }
              return allFilteredMeasures.stream()
                  .filter(
                      m ->
                          m.getAdditionalCode().isEmpty() || m.getAdditionalCode().get().equals(ac))
                  .collect(Collectors.toList());
            })
        .orElse(allFilteredMeasures);
  }

  private String convertToEUIfNeeded(String countryCode) {
    if (CountryHelper.isEUCountry(countryCode)) {
      return "EU";
    }
    return countryCode;
  }
}
