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

import static java.lang.String.format;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties.CONTEXT_ROOT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDate;
import java.util.Arrays;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang3.ObjectUtils;
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
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.AdditionalCodesService;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.time.Clock;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.response.AdditionalCodesResponse;

@RestController
@RequestMapping(value = CONTEXT_ROOT)
@Validated
public class AdditionalCodesController {

  private final AdditionalCodesService additionalCodesService;
  private final Clock clock;

  @Autowired
  public AdditionalCodesController(AdditionalCodesService additionalCodesService, Clock clock) {
    this.additionalCodesService = additionalCodesService;
    this.clock = clock;
  }

  @GetMapping("/commodities/{commodityCode}/additional-codes")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Additional codes for a given commodity code.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdditionalCodesResponse.class)))
      })
  @Operation(
      summary =
          "Gets additional codes for a given commodity, filtered by trade type and country of origin.",
      description =
          "Gets additional codes for a given commodity, filtered by trade type and country of origin.")
  public Mono<AdditionalCodesResponse> getAdditionalCodes(
      @Parameter(description = "The code of the commodity", required = true, example = "1006101000")
          @PathVariable("commodityCode")
          @Pattern(regexp = "^\\d{8}|\\d{10}$")
          String commodityCode,
      @Parameter(
              description = "The type of trade (import/export)",
              required = true,
              example = "import")
          @NotNull
          TradeType tradeType,
      @Parameter(description = "ISO 3166-1 Origin Country code", required = true, example = "CN")
          @NotNull
          @Pattern(regexp = "^\\w{2}$")
          String originCountry,
      @Parameter(
              description = "ISO 3166-1 Destination Country code",
              required = true,
              example = "CN")
          @NotNull
          @Pattern(regexp = "^\\w{2}$")
          String destinationCountry,
      @Parameter(description = "Import date", example = "2021-09-30")
          @DateTimeFormat(pattern = "yyyy-MM-dd")
          @RequestParam(required = false)
          LocalDate importDate,
      @Parameter(description = "Date of Trade", example = "2021-09-30")
          @DateTimeFormat(pattern = "yyyy-MM-dd")
          @RequestParam(required = false)
          LocalDate tradeDate) {
    if (tradeType == TradeType.IMPORT
        && Arrays.stream(UkCountry.values())
            .noneMatch(ukCountry -> destinationCountry.equals(ukCountry.name()))) {
      return Mono.error(
          new ValidationException(
              "destinationCountry",
              format("Destination country %s is not a valid UK country", destinationCountry)));
    }

    if (tradeType == TradeType.EXPORT
        && Arrays.stream(UkCountry.values())
            .noneMatch(ukCountry -> originCountry.equals(ukCountry.name()))) {
      return Mono.error(
          new ValidationException(
              "originCountry",
              format("Origin country %s is not a valid UK country", originCountry)));
    }

    return additionalCodesService
        .getAdditionalCodes(
            commodityCode,
            tradeType,
            originCountry,
            destinationCountry,
            ObjectUtils.firstNonNull(importDate, tradeDate, clock.currentLocalDate()))
        .map(AdditionalCodesResponse::new);
  }
}
