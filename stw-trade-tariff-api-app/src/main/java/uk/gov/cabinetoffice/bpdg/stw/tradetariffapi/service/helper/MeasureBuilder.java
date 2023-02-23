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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.helper;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData.DutyCalculatorAdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityAdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityGeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasure;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityMeasureType;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.DutyExpression;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.AdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.GeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@Component
@Slf4j
public class MeasureBuilder {

  public List<Measure> from(TradeTariffCommodityResponse tradeTariffCommodityResponse) {
    return tradeTariffCommodityResponse.getMeasures().stream()
        .filter(m -> m.isImport() || m.isExport())
        .map(
            m ->
                Measure.builder()
                    .id(m.getId())
                    .taxMeasure(m.isVAT() || m.isExcise())
                    .measureType(findMeasureType(m, tradeTariffCommodityResponse.getMeasureTypes()))
                    .geographicalArea(
                        findGeographicalArea(
                            m, tradeTariffCommodityResponse.getGeographicalAreas()))
                    .measureConditions(
                        findMeasureConditions(
                            m, tradeTariffCommodityResponse.getMeasureConditions()))
                    .excludedCountries(m.getExcludedCountries())
                    .additionalCode(
                        maybeFindAdditionalCode(
                            m,
                            tradeTariffCommodityResponse.getAdditionalCodes(),
                            tradeTariffCommodityResponse
                                .getData()
                                .getDutyCalculatorAdditionalCodes()))
                    .legalActId(m.getLegalActId())
                    .dutyValue(findDutyRate(m, tradeTariffCommodityResponse))
                    .applicableTradeTypes(
                        m.isImport() && m.isExport()
                            ? List.of(TradeType.IMPORT, TradeType.EXPORT)
                            : m.isImport() ? List.of(TradeType.IMPORT) : List.of(TradeType.EXPORT))
                    .quotaNumber(m.getQuotaNumber())
                    .build())
        .collect(Collectors.toList());
  }

  private String findDutyRate(
      CommodityMeasure m, TradeTariffCommodityResponse tradeTariffCommodityResponse) {
    Optional<DutyExpression> dutyExpression =
        tradeTariffCommodityResponse.getDutyRates().stream()
            .filter(rate -> rate.getId().equals(m.getDutyExpressionId()))
            .findFirst();
    return dutyExpression.map(DutyExpression::getBase).orElse(null);
  }

  private AdditionalCode maybeFindAdditionalCode(
      CommodityMeasure measure,
      List<CommodityAdditionalCode> additionalCodes,
      List<DutyCalculatorAdditionalCode> additionalCodesOverlay) {

    AdditionalCode maybeAdditionalCode = null;

    if (StringUtils.isNotBlank(measure.getAdditionalCodeId())) {
      maybeAdditionalCode =
          additionalCodes.stream()
              .filter(ac -> ac.getId().equals(measure.getAdditionalCodeId()))
              .findFirst()
              .map(
                  cac ->
                      AdditionalCode.builder()
                          .code(cac.getCode())
                          .description(
                              findOverlayIfDefined(cac.getCode(), additionalCodesOverlay)
                                  .orElse(cac.getDescription()))
                          .build())
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          String.format(
                              "Could not find an additionalCode element of id %s in response.",
                              measure.getAdditionalCodeId())));
    }

    return maybeAdditionalCode;
  }

  private Optional<String> findOverlayIfDefined(
      String additionalCodeId, List<DutyCalculatorAdditionalCode> additionalCodesOverlay) {
    return Optional.ofNullable(additionalCodesOverlay).orElse(List.of()).stream()
        .filter(overlay -> additionalCodeId.equals(overlay.getCode()))
        .map(DutyCalculatorAdditionalCode::getOverlay)
        .findFirst();
  }

  private List<MeasureCondition> findMeasureConditions(
      CommodityMeasure measure, List<CommodityMeasureCondition> measureConditions) {
    return measure.getMeasureConditionIds().stream()
        .map(
            measureConditionId ->
                measureConditions.stream()
                    .filter(m -> m.getId().equals(measureConditionId))
                    .findFirst()
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                String.format(
                                    "Cannot find measure condition for measure condition %s",
                                    measureConditionId))))
        .map(
            measureCondition ->
                MeasureCondition.builder()
                    .id(measureCondition.getId())
                    .action(measureCondition.getAction())
                    .condition(measureCondition.getCondition())
                    .conditionCode(MeasureConditionCode.from(measureCondition.getConditionCode()))
                    .documentCode(measureCondition.getDocumentCode())
                    .dutyExpression(measureCondition.getDutyExpression())
                    .requirement(measureCondition.getRequirement())
                    .build())
        .collect(Collectors.toList());
  }

  private MeasureType findMeasureType(CommodityMeasure m, List<CommodityMeasureType> measureTypes) {
    return measureTypes.stream()
        .filter(measureType -> measureType.getId().equals(m.getMeasureTypeId()))
        .findFirst()
        .map(
            tradeTariffMeasureType ->
                MeasureType.builder()
                    .id(tradeTariffMeasureType.getId())
                    .seriesId(tradeTariffMeasureType.getSeriesId())
                    .description(tradeTariffMeasureType.getDescription())
                    .build())
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format("Cannot find measure type for measure %s", m.getId())));
  }

  private GeographicalArea findGeographicalArea(
      CommodityMeasure m, List<CommodityGeographicalArea> geographicalAreas) {
    return geographicalAreas.stream()
        .filter(geographicalArea -> geographicalArea.getId().equals(m.getGeographicalAreaId()))
        .findFirst()
        .map(
            geographicalArea ->
                GeographicalArea.builder()
                    .id(geographicalArea.getId())
                    .description(geographicalArea.getDescription())
                    .childrenGeographicalAreas(
                        new HashSet<>(geographicalArea.getChildrenGeographicalAreas()))
                    .build())
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format("Cannot find geographical area for measure %s", m.getId())));
  }
}
