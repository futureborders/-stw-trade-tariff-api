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

import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.CommoditiesApiVersion;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.TradeTariffApi;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.TradeTariffError;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class TradeTariffApiGatewayTest {

  @Mock private TradeTariffApi tradeTariffApi;

  @InjectMocks private TradeTariffApiGateway tradeTariffApiGateway;

  @ParameterizedTest
  @EnumSource(
      value = UkCountry.class,
      names = {"GB", "XI"})
  void shouldThrowExceptionWhenCommodityNotFound(UkCountry destinationCountry) {
    var importDate = LocalDate.now();
    String commodityCode = "4103900000";
    CommoditiesApiVersion commoditiesApiVersion =
        destinationCountry == UkCountry.GB
            ? CommoditiesApiVersion.COMMODITIES_GB_V2
            : CommoditiesApiVersion.COMMODITIES_XI_V2;
    when(tradeTariffApi.getCommodity(commodityCode, importDate, commoditiesApiVersion))
        .thenReturn(
            Mono.just(
                TradeTariffCommodityResponse.builder()
                    .errors(
                        List.of(TradeTariffError.builder().detail("commodity not found").build()))
                    .build()));

    StepVerifier.create(
            tradeTariffApiGateway.getCommodity(commodityCode, importDate, destinationCountry))
        .expectErrorMatches(
            ex ->
                ex instanceof ResourceNotFoundException
                    && "Resource 'Commodity' not found with id '4103900000'"
                        .equals(ex.getMessage()))
        .verify();
  }

  @ParameterizedTest
  @EnumSource(
      value = UkCountry.class,
      names = {"GB", "XI"})
  void shouldReturnAdditionalCodes(UkCountry destinationCountry) {
    var importDate = LocalDate.now();
    String commodityCode = "1234567890";
    CommoditiesApiVersion commoditiesApiVersion =
        destinationCountry == UkCountry.GB
            ? CommoditiesApiVersion.COMMODITIES_GB_V2
            : CommoditiesApiVersion.COMMODITIES_XI_V2;
    TradeTariffCommodityResponse tariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
            .data(
                TradeTariffCommodityResponseData.builder()
                    .id("1234")
                    .type("commodity")
                    .formattedDescription("description")
                    .goodsNomenclatureItemId(commodityCode)
                    .build())
            .build();
    when(tradeTariffApi.getCommodity(commodityCode, importDate, commoditiesApiVersion))
        .thenReturn(Mono.just(tariffCommodityResponse));

    StepVerifier.create(
            tradeTariffApiGateway.getCommodity(commodityCode, importDate, destinationCountry))
        .expectNext(tariffCommodityResponse)
        .verifyComplete();
  }
}
