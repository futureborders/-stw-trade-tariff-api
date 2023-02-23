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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.CommodityMeasuresRequest;

class TaxApplicabilityServiceTest {

  private final TaxApplicabilityService taxApplicabilityService = new TaxApplicabilityService();

  @Test
  void shouldSetApplicableToFalseForImportingToGBFromNorthernIsland() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder()
        .destinationCountry(UkCountry.GB)
        .originCountry("XI")
        .build();
    TradeTariffCommodityResponse response = TradeTariffCommodityResponse.builder().build();

    // when && then
    StepVerifier.create(taxApplicabilityService.isApplicable(request, response))
      .expectNext(false)
      .verifyComplete();
  }

  @Test
  void shouldSetApplicableToFalseForImportsFromROWToGBWithNoMFNAndTradeRemedies() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder()
        .destinationCountry(UkCountry.GB)
        .originCountry("DE")
        .build();
    uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
      taxAndDutyApi =
      uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
        .builder()
        .hasTradeRemedies(false)
        .hasMostFavouredNationDuty(false)
        .build();
    TradeTariffCommodityResponseData data =
      TradeTariffCommodityResponseData.builder().taxAndDuty(taxAndDutyApi).build();
    TradeTariffCommodityResponse response =
      TradeTariffCommodityResponse.builder().data(data).build();

    // when && then
    StepVerifier.create(taxApplicabilityService.isApplicable(request, response))
      .expectNext(false)
      .verifyComplete();
  }

  @Test
  void shouldSetApplicableToTrueForImportsFromROWToGBWithTradeRemedies() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder()
        .destinationCountry(UkCountry.GB)
        .originCountry("DE")
        .build();
    uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
      taxAndDutyApi =
      uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
        .builder()
        .hasTradeRemedies(true)
        .hasMostFavouredNationDuty(false)
        .build();
    TradeTariffCommodityResponseData data =
      TradeTariffCommodityResponseData.builder().taxAndDuty(taxAndDutyApi).build();
    TradeTariffCommodityResponse response =
      TradeTariffCommodityResponse.builder().data(data).build();

    // when && then
    StepVerifier.create(taxApplicabilityService.isApplicable(request, response))
      .expectNext(true)
      .verifyComplete();
  }

  @Test
  void shouldSetApplicableToTrueForImportsFromROWToGBWithNoTradeRemediesButHasMFNDuty() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder()
        .destinationCountry(UkCountry.GB)
        .originCountry("DE")
        .build();
    uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
      taxAndDutyApi =
      uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
        .builder()
        .hasTradeRemedies(false)
        .hasMostFavouredNationDuty(true)
        .build();
    TradeTariffCommodityResponseData data =
      TradeTariffCommodityResponseData.builder().taxAndDuty(taxAndDutyApi).build();
    TradeTariffCommodityResponse response =
      TradeTariffCommodityResponse.builder().data(data).build();

    // when && then
    StepVerifier.create(taxApplicabilityService.isApplicable(request, response))
      .expectNext(true)
      .verifyComplete();
  }

  @Test
  void shouldSetApplicableToFalseForImportsFromEUToNorthernIreland() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder()
        .destinationCountry(UkCountry.XI)
        .originCountry("DE")
        .build();
    uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
      taxAndDutyApi =
      uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
        .builder()
        .hasTradeRemedies(true)
        .hasMostFavouredNationDuty(true)
        .build();
    TradeTariffCommodityResponseData data =
      TradeTariffCommodityResponseData.builder().taxAndDuty(taxAndDutyApi).build();
    TradeTariffCommodityResponse response =
      TradeTariffCommodityResponse.builder().data(data).build();

    // when && then
    StepVerifier.create(taxApplicabilityService.isApplicable(request, response))
      .expectNext(false)
      .verifyComplete();
  }

  @Test
  void shouldSetApplicableToFalseForImportsFromIrelandToNorthernIreland() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder()
        .destinationCountry(UkCountry.XI)
        .originCountry("IE")
        .build();
    uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
      taxAndDutyApi =
      uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
        .builder()
        .hasTradeRemedies(true)
        .hasMostFavouredNationDuty(true)
        .build();
    TradeTariffCommodityResponseData data =
      TradeTariffCommodityResponseData.builder().taxAndDuty(taxAndDutyApi).build();
    TradeTariffCommodityResponse response =
      TradeTariffCommodityResponse.builder().data(data).build();

    // when && then
    StepVerifier.create(taxApplicabilityService.isApplicable(request, response))
      .expectNext(false)
      .verifyComplete();
  }

  @Test
  void shouldSetApplicableToTrueForImportsFromGBToNorthernIrelandWhenNoTradeRemediesButHasMFNDuty() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder()
        .destinationCountry(UkCountry.XI)
        .originCountry("GB")
        .build();
    uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
      taxAndDutyApi =
      uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
        .builder()
        .hasTradeRemedies(false)
        .hasMostFavouredNationDuty(true)
        .build();
    TradeTariffCommodityResponseData data =
      TradeTariffCommodityResponseData.builder().taxAndDuty(taxAndDutyApi).build();
    TradeTariffCommodityResponse response =
      TradeTariffCommodityResponse.builder().data(data).build();

    // when && then
    StepVerifier.create(taxApplicabilityService.isApplicable(request, response))
      .expectNext(true)
      .verifyComplete();
  }

  @Test
  void shouldSetApplicableToTrueForImportsFromGBToNorthernIrelandWhenTradeRemediesButNoMFNDuty() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder()
        .destinationCountry(UkCountry.XI)
        .originCountry("GB")
        .build();
    uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
      taxAndDutyApi =
      uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
        .builder()
        .hasTradeRemedies(true)
        .hasMostFavouredNationDuty(false)
        .build();
    TradeTariffCommodityResponseData data =
      TradeTariffCommodityResponseData.builder().taxAndDuty(taxAndDutyApi).build();
    TradeTariffCommodityResponse response =
      TradeTariffCommodityResponse.builder().data(data).build();

    // when && then
    StepVerifier.create(taxApplicabilityService.isApplicable(request, response))
      .expectNext(true)
      .verifyComplete();
  }

  @Test
  void shouldSetApplicableToFalseForImportsFromGBToNorthernIrelandWhenThereAreNoTradeRemediesAndNoMFNDuty() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder()
        .destinationCountry(UkCountry.XI)
        .originCountry("GB")
        .build();
    uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
      taxAndDutyApi =
      uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
        .builder()
        .hasTradeRemedies(false)
        .hasMostFavouredNationDuty(false)
        .build();
    TradeTariffCommodityResponseData data =
      TradeTariffCommodityResponseData.builder().taxAndDuty(taxAndDutyApi).build();
    TradeTariffCommodityResponse response =
      TradeTariffCommodityResponse.builder().data(data).build();

    // when && then
    StepVerifier.create(taxApplicabilityService.isApplicable(request, response))
      .expectNext(false)
      .verifyComplete();
  }

  @Test
  void shouldSetApplicableToTrueForImportsFromROWToNorthernIreland() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder()
        .destinationCountry(UkCountry.XI)
        .originCountry("IN")
        .build();
    uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
      taxAndDutyApi =
      uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TaxAndDuty
        .builder()
        .hasTradeRemedies(false)
        .hasMostFavouredNationDuty(false)
        .build();
    TradeTariffCommodityResponseData data =
      TradeTariffCommodityResponseData.builder().taxAndDuty(taxAndDutyApi).build();
    TradeTariffCommodityResponse response =
      TradeTariffCommodityResponse.builder().data(data).build();

    // when && then
    StepVerifier.create(taxApplicabilityService.isApplicable(request, response))
      .expectNext(true)
      .verifyComplete();
  }

  @Test
  void shouldThrowExceptionWhenDestinationCountryIsNotKnownOrNotSet() {
    // given
    CommodityMeasuresRequest request =
      CommodityMeasuresRequest.builder().destinationCountry(null).originCountry("IN").build();
    TradeTariffCommodityResponse response = TradeTariffCommodityResponse.builder().build();

    // when & then
    assertThatExceptionOfType(IllegalStateException.class)
      .isThrownBy(() -> taxApplicabilityService.isApplicable(request, response))
      .withMessage("Not a recognised destination country 'null'");
  }
}
