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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.AdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.helper.MeasureBuilder;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class AdditionalCodesService {

  private final TradeTariffApiGateway tradeTariffApiGateway;
  private final MeasureBuilder measureBuilder;
  private final MeasureFilterer measureFilterer;

  public Mono<List<AdditionalCode>> getAdditionalCodes(
      String commodityCode,
      TradeType tradeType,
      String originCountry,
      String destinationCountry,
      LocalDate dateOfTrade) {

    log.debug(
        String.format(
            "fetching additional codes for [commodity: %s, tradeType: %s, originCountry: %s, destinationCountry: %s]",
            commodityCode, tradeType, originCountry, destinationCountry));

    return this.tradeTariffApiGateway
        .getCommodity(
            commodityCode,
            dateOfTrade,
            tradeType == TradeType.IMPORT
                ? UkCountry.valueOf(destinationCountry)
                : UkCountry.valueOf(originCountry))
        .flatMap(
            response ->
                Mono.just(
                    extractAdditionalCodes(
                        response,
                        tradeType,
                        tradeType == TradeType.IMPORT ? originCountry : destinationCountry,
                        commodityCode)));
  }

  private List<AdditionalCode> extractAdditionalCodes(
      TradeTariffCommodityResponse response,
      TradeType tradeType,
      String tradeDestinationCountry,
      String commodityCode) {
    List<Measure> filteredMeasures =
        measureFilterer.getRestrictiveMeasures(
            measureBuilder.from(response, commodityCode), tradeType, tradeDestinationCountry);

    return filteredMeasures.stream()
        .map(Measure::getAdditionalCode)
        .flatMap(Optional::stream)
        .collect(Collectors.toList());
  }
}
