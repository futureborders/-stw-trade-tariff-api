/*
 * Copyright 2021 Crown Copyright (Single Trade Window)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.AdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Duty;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Quota;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tariff;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TariffAndTaxes;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tax;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.TariffAndTaxesService;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.TariffAndTaxesRequest;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.response.TariffAndTaxesResponse;

@RestController
@RequestMapping(value = CONTEXT_ROOT)
@Validated
public class TariffAndTaxesController {

  private static final Comparator<Tariff> TARIFF_COMPARATOR =
      (o1, o2) ->
          Comparator.comparing(Tariff::getMeasureTypeId)
              .thenComparing(tariff -> tariff.getGeographicalArea().getId())
              .thenComparing(
                  tariff -> tariff.getAdditionalCode().map(AdditionalCode::getCode).orElse(null),
                  Comparator.nullsLast(Comparator.naturalOrder()))
              .thenComparing(
                  tariff -> tariff.getQuota().map(Quota::getNumber).orElse(null),
                  Comparator.nullsLast(Comparator.naturalOrder()))
              .compare(o1, o2);

  private static final Comparator<Tax> TAXES_COMPARATOR =
      (o1, o2) ->
          Comparator.comparing(Tax::getMeasureTypeId)
              .thenComparing(tax -> tax.getGeographicalArea().getId())
              .thenComparing(
                  tax -> tax.getAdditionalCode().map(AdditionalCode::getCode).orElse(null),
                  Comparator.nullsLast(Comparator.naturalOrder()))
              .compare(o1, o2);

  private final TariffAndTaxesService tariffAndTaxesService;

  @Autowired
  public TariffAndTaxesController(TariffAndTaxesService tariffAndTaxesService) {
    this.tariffAndTaxesService = tariffAndTaxesService;
  }

  @GetMapping("/commodities/{commodityCode}/duties")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Duties for a given commodity code.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TariffAndTaxesResponse.class)))
      })
  @Operation(
      summary = "Gets Duties for a given commodity.",
      description =
          "Gets tariffs and taxes for a given commodity code based on trade type and country of origin.")
  public Mono<TariffAndTaxesResponse> getTariffAndTaxes(
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
      @Parameter(description = "Locale", example = "EN or CY") @RequestParam(required = false)
          Locale locale,
      @Parameter(description = "Import date", example = "2021-09-30")
          @DateTimeFormat(pattern = "yyyy-MM-dd")
          @RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now()}")
          LocalDate importDate) {
    TariffAndTaxesRequest request =
        TariffAndTaxesRequest.builder()
            .commodityCode(tidyCommodityCode(commodityCode))
            .originCountry(originCountry)
            .destinationCountry(destinationCountry)
            .tradeType(tradeType)
            .importDate(importDate)
            .locale(Optional.ofNullable(locale).orElse(Locale.EN))
            .build();
    return tariffAndTaxesService.getTariffAndTaxes(request).map(this::convertToResponse);
  }

  private TariffAndTaxesResponse convertToResponse(TariffAndTaxes tariffAndTaxes) {
    Map<Boolean, List<Duty>> collect =
        tariffAndTaxes.getDuties().stream()
            .collect(Collectors.partitioningBy(Tariff.class::isInstance));
    return TariffAndTaxesResponse.builder()
        .taxes(
            collect.get(Boolean.FALSE).stream()
                .map(Tax.class::cast)
                .sorted(TAXES_COMPARATOR)
                .collect(Collectors.toList()))
        .tariffs(
            collect.get(Boolean.TRUE).stream()
                .map(Tariff.class::cast)
                .sorted(TARIFF_COMPARATOR)
                .collect(Collectors.toList()))
        .build();
  }
}
