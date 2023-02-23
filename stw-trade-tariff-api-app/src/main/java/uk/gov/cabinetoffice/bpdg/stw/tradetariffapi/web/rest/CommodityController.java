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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest;

import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties.CONTEXT_ROOT;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.CommodityHelper.tidyCommodityCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDate;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UserType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.CommodityService;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.CommodityMeasuresRequest;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.response.CommodityMeasuresResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.response.MeasurePayload;

@RestController
@RequestMapping(value = CONTEXT_ROOT)
@Validated
public class CommodityController {

  private final CommodityService commodityService;

  @Autowired
  public CommodityController(CommodityService commodityService) {
    this.commodityService = commodityService;
  }

  @GetMapping("/commodities/{commodityCode}/measures")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Measures for a given commodity code.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CommodityMeasuresResponse.class)))
      })
  @Operation(
      summary = "Gets measures for a given commodity.",
      description =
          "Gets measures for a given commodity code based on trade type and country of origin.")
  public Mono<CommodityMeasuresResponse> getCommodityMeasures(
      @Parameter(description = "The code of the commodity", required = true, example = "1006101000")
          @PathVariable("commodityCode")
          @Pattern(regexp = "^\\d{8}|\\d{10}$")
          String commodityCode,
      @Parameter(
              description = "The type of trade (import/export)",
              required = true,
              example = "import")
          @NotNull
          @RequestParam(required = false)
          TradeType tradeType,
      @Parameter(description = "ISO 3166-1 Origin Country code", required = true, example = "CN")
          @NotNull
          @Pattern(regexp = "^\\w{2}$")
          @RequestParam(required = false)
          String originCountry,
      @Parameter(
              description = "ISO 3166-1 Destination Country code",
              required = true,
              example = "GB or XI")
          @NotNull
          @RequestParam(required = false)
          UkCountry destinationCountry,
      @Parameter(
              description = "User type (trader or intermediary)",
              required = true,
              example = "trader")
          @NotNull
          @RequestParam(required = false)
          UserType userType,
      @Parameter(description = "Additional code", example = "4204")
          @Pattern(regexp = "^\\d{4}$")
          @RequestParam(required = false)
          String additionalCode,
      @Parameter(description = "Import date", example = "2021-09-30")
          @DateTimeFormat(pattern = "yyyy-MM-dd")
          @RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now()}")
          LocalDate importDate) {
    CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
            .commodityCode(tidyCommodityCode(commodityCode))
            .originCountry(originCountry)
            .destinationCountry(destinationCountry)
            .tradeType(tradeType)
            .userType(userType)
            .additionalCode(additionalCode)
            .importDate(importDate)
            .build();
    return this.commodityService.getCommodityMeasures(request).map(cm -> buildMeasuresResponse(cm, tradeType));
  }

  private CommodityMeasuresResponse buildMeasuresResponse(
      uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.CommodityMeasures cm, TradeType tradeType) {
    return CommodityMeasuresResponse.builder()
        .commodityCode(cm.getCommodityCode())
        .commodityDescription(cm.getCommodityDescription())
        .measures(
            cm.getMeasures().stream()
                .filter(m -> m.getApplicableTradeTypes().contains(tradeType))
                .map(
                    m ->
                        MeasurePayload.builder()
                            .id(m.getId())
                            .tradeType(tradeType)
                            .measureType(m.getMeasureType())
                            .geographicalArea(m.getGeographicalArea())
                            .additionalCode(m.getAdditionalCode().orElse(null))
                            .measureConditions(m.getMeasureConditions())
                            .build())
                .collect(Collectors.toList()))
        .signpostingContents(cm.getSignpostingContents())
        .prohibitions(cm.getProhibitions())
        .commodityHierarchy(cm.getCommodityHierarchy())
        .taxAndDuty(cm.getTaxAndDuty())
        .build();
  }

}
