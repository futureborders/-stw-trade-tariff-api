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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service;

import static java.lang.String.format;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Tax;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TariffAndTaxes;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.ResourceNotFoundException;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.helper.MeasureBuilder;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.TariffAndTaxesRequest;

@ExtendWith(MockitoExtension.class)
class TariffAndTaxesServiceTest {

  private static final String COMMODITY_CODE = "0123456789";

  @Mock private TradeTariffApiGateway tradeTariffApiGateway;
  @Mock private MeasureFilterer measureFilterer;
  @Mock private MeasureBuilder measureBuilder;
  @Mock private DutyMeasureService dutyMeasureService;

  @InjectMocks private TariffAndTaxesService tariffAndTaxesService;

  @Test
  @DisplayName("Taxes should be displayed for commodities imported to GB")
  void shouldReturnTaxesApplicableToTheCommodityForImportsToGB() {
    // given
    LocalDate dateOfImport = LocalDate.now();
    UkCountry destinationCountry = UkCountry.GB;
    TariffAndTaxesRequest tariffAndTaxesRequest =
      TariffAndTaxesRequest.builder()
        .destinationCountry(destinationCountry)
        .commodityCode(COMMODITY_CODE)
        .importDate(dateOfImport)
        .build();

    TradeTariffCommodityResponse tradeTariffCommodityResponse =
      TradeTariffCommodityResponse.builder()
        .data(TradeTariffCommodityResponseData.builder().id("1234").type("commodity").build())
        .build();

    Measure restrictiveMeasure =
      Measure.builder()
        .measureType(MeasureType.builder().seriesId("A").build())
        .taxMeasure(false)
        .applicableTradeTypes(List.of(TradeType.IMPORT))
        .build();
    Measure dutyMeasure =
      Measure.builder()
        .measureType(MeasureType.builder().seriesId("C").build())
        .taxMeasure(true)
        .applicableTradeTypes(List.of(TradeType.IMPORT))
        .build();

    Tax tax = Tax.builder().build();

    List<Measure> measures = List.of(restrictiveMeasure, dutyMeasure);

    when(tradeTariffApiGateway.getCommodity(COMMODITY_CODE, dateOfImport, destinationCountry))
      .thenReturn(Mono.just(tradeTariffCommodityResponse));
    when(measureBuilder.from(tradeTariffCommodityResponse)).thenReturn(measures);
    when(measureFilterer.getTaxAndDutyMeasures(measures, tariffAndTaxesRequest.getTradeType(), tariffAndTaxesRequest.getOriginCountry()))
      .thenReturn(List.of(dutyMeasure));
    when(dutyMeasureService.getTariffsAndTaxesMeasures(
      List.of(dutyMeasure), tariffAndTaxesRequest.getDestinationCountry()))
      .thenReturn(Flux.just(tax));
    // when and then
    StepVerifier.create(tariffAndTaxesService.getTariffAndTaxes(tariffAndTaxesRequest))
      .expectNext(
        TariffAndTaxes.builder()
          .duties(List.of(tax))
          .build())
      .verifyComplete();
  }

  @Test
  @DisplayName("Taxes should not be displayed for commodities imported to XI as they are complex")
  void shouldReturnEmptyTaxesForImportsToNorthernIreland() {
    // given
    LocalDate dateOfImport = LocalDate.now();
    TariffAndTaxesRequest tariffAndTaxesRequest =
      TariffAndTaxesRequest.builder()
        .destinationCountry(UkCountry.XI)
        .commodityCode(COMMODITY_CODE)
        .importDate(dateOfImport)
        .build();

    // when and then
    StepVerifier.create(tariffAndTaxesService.getTariffAndTaxes(tariffAndTaxesRequest))
      .expectNext(
        TariffAndTaxes.builder()
          .duties(List.of())
          .build())
      .verifyComplete();
  }

  @Test
  void shouldReturnResourceNotFoundExceptionWhenCommodityNotFound() {
    // given
    LocalDate dateOfImport = LocalDate.now();
    UkCountry destinationCountry = UkCountry.GB;
    TariffAndTaxesRequest tariffAndTaxesRequest =
      TariffAndTaxesRequest.builder()
        .destinationCountry(destinationCountry)
        .commodityCode(COMMODITY_CODE)
        .importDate(dateOfImport)
        .build();

    when(tradeTariffApiGateway.getCommodity(COMMODITY_CODE, dateOfImport, destinationCountry))
      .thenReturn(Mono.error(new ResourceNotFoundException("Commodity", COMMODITY_CODE)));

    // when and then
    StepVerifier.create(tariffAndTaxesService.getTariffAndTaxes(tariffAndTaxesRequest))
      .expectErrorMatches(
        error ->
          error instanceof ResourceNotFoundException
            && error
            .getMessage()
            .equals(
              format("Resource 'Commodity' not found with id '%s'", COMMODITY_CODE)))
      .verify();
  }
}
