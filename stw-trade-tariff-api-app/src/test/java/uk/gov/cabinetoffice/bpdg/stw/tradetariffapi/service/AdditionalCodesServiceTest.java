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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.AdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.ResourceNotFoundException;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.helper.MeasureBuilder;

@ExtendWith(MockitoExtension.class)
class AdditionalCodesServiceTest {

  final Measure MEASURE1 =
    Measure.builder()
      .id("1")
      .applicableTradeTypes(List.of(TradeType.IMPORT))
      .measureType(MeasureType.builder().id("277").seriesId("B").build())
      .additionalCode(AdditionalCode.builder().code("4200").description("Procyon lotor").build())
      .build();
  final Measure MEASURE2 =
    Measure.builder()
      .id("1")
      .applicableTradeTypes(List.of(TradeType.IMPORT))
      .measureType(MeasureType.builder().id("278").seriesId("B").build())
      .additionalCode(AdditionalCode.builder().code("4201").description("Canis lupus").build())
      .build();
  final Measure MEASURE3 =
    Measure.builder()
      .id("1")
      .applicableTradeTypes(List.of(TradeType.IMPORT))
      .measureType(MeasureType.builder().id("279").seriesId("B").build())
      .additionalCode(AdditionalCode.builder().code("4202").description("Martes zibellina").build())
      .build();
  final Measure MEASURE4 =
    Measure.builder()
      .id("1")
      .applicableTradeTypes(List.of(TradeType.IMPORT))
      .measureType(MeasureType.builder().id("280").seriesId("B").build())
      .additionalCode(AdditionalCode.builder().code("4999").description("Other").build())
      .build();

  private @Mock TradeTariffApiGateway tradeTariffApiGateway;
  private @Mock MeasureBuilder measureBuilder;
  private @Mock MeasureFilterer measureFilterer;
  private @InjectMocks AdditionalCodesService additionalCodesService;
  private final String commodityCode = "4103900000";
  private final TradeTariffCommodityResponse tariffCommodityResponse =
    TradeTariffCommodityResponse.builder()
      .data(
        TradeTariffCommodityResponseData.builder()
          .id("1234")
          .type("commodity")
          .formattedDescription("description")
          .goodsNomenclatureItemId(commodityCode)
          .build())
      .build();
  private final TradeType tradeType = TradeType.IMPORT;
  private final String origin = "TR";
  private final UkCountry destination = UkCountry.GB;

  @Test
  void shouldReturnAdditionalCodesForImports() {
    var tradeDate = LocalDate.now();
    when(tradeTariffApiGateway.getCommodity(
      commodityCode, tradeDate, destination))
      .thenReturn(Mono.just(tariffCommodityResponse));

    List<Measure> builtMeasures = List.of(MEASURE1, MEASURE2, MEASURE3, MEASURE4);
    when(measureBuilder.from(tariffCommodityResponse)).thenReturn(builtMeasures);
    when(measureFilterer.getRestrictiveMeasures(builtMeasures, tradeType, origin))
      .thenReturn(builtMeasures);
    List<AdditionalCode> additionalCodes =
      additionalCodesService
        .getAdditionalCodes(commodityCode, tradeType, origin, destination.name(), tradeDate)
        .block();

    assertThat(additionalCodes)
      .containsExactlyInAnyOrder(
        AdditionalCode.builder().code("4200").description("Procyon lotor").build(),
        AdditionalCode.builder().code("4201").description("Canis lupus").build(),
        AdditionalCode.builder().code("4202").description("Martes zibellina").build(),
        AdditionalCode.builder().code("4999").description("Other").build());
  }

  @Test
  void shouldReturnAdditionalCodesForExports() {
    UkCountry origin = UkCountry.XI;
    String destination = "CH";
    var tradeDate = LocalDate.now();
    when(tradeTariffApiGateway.getCommodity(
      commodityCode, tradeDate, origin))
      .thenReturn(Mono.just(tariffCommodityResponse));

    List<Measure> builtMeasures = List.of(MEASURE1, MEASURE2, MEASURE3, MEASURE4);
    when(measureBuilder.from(tariffCommodityResponse)).thenReturn(builtMeasures);
    when(measureFilterer.getRestrictiveMeasures(builtMeasures, TradeType.EXPORT, destination))
      .thenReturn(builtMeasures);
    List<AdditionalCode> additionalCodes =
      additionalCodesService
        .getAdditionalCodes(commodityCode, TradeType.EXPORT, origin.name(), destination, tradeDate)
        .block();

    assertThat(additionalCodes)
      .containsExactlyInAnyOrder(
        AdditionalCode.builder().code("4200").description("Procyon lotor").build(),
        AdditionalCode.builder().code("4201").description("Canis lupus").build(),
        AdditionalCode.builder().code("4202").description("Martes zibellina").build(),
        AdditionalCode.builder().code("4999").description("Other").build());
  }

  @Test
  void shouldReturnEmptyWhenNoAdditionalCodesExist() {
    var importDate = LocalDate.now();
    when(tradeTariffApiGateway.getCommodity(
      commodityCode, importDate, destination))
      .thenReturn(Mono.just(tariffCommodityResponse));

    List<Measure> builtMeasures = List.of(MEASURE1, MEASURE2, MEASURE3, MEASURE4);
    when(measureBuilder.from(tariffCommodityResponse)).thenReturn(builtMeasures);
    when(measureFilterer.getRestrictiveMeasures(builtMeasures, tradeType, origin))
      .thenReturn(Collections.emptyList());

    List<AdditionalCode> additionalCodes =
      additionalCodesService
        .getAdditionalCodes(commodityCode, tradeType, origin, destination.name(), importDate)
        .block();

    assertThat(additionalCodes).isEmpty();
  }

  @Test
  void shouldThrowExceptionWhenCommodityNotFound() {
    var importDate = LocalDate.now();
    when(tradeTariffApiGateway.getCommodity(
      commodityCode, importDate, destination))
      .thenReturn(
        Mono.error(new ResourceNotFoundException("Commodity", "4103900000")));

    assertThatExceptionOfType(ResourceNotFoundException.class)
      .isThrownBy(
        () ->
          additionalCodesService
            .getAdditionalCodes(commodityCode, tradeType, origin, destination.name(), importDate)
            .block())
      .withMessage("Resource 'Commodity' not found with id '4103900000'");
  }
}
