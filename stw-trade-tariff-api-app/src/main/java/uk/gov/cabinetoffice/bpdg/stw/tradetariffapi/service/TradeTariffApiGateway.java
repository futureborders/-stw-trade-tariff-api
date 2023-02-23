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

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.CommoditiesApiVersion;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.TradeTariffApi;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.ResourceNotFoundException;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class TradeTariffApiGateway {

  private final TradeTariffApi tradeTariffApi;

  public Mono<TradeTariffCommodityResponse> getCommodity(String commodityCode, LocalDate dateOfTrade, UkCountry apiCountry) {

    final CommoditiesApiVersion commoditiesApiVersion =
        apiCountry == UkCountry.GB
            ? CommoditiesApiVersion.COMMODITIES_GB_V2
            : CommoditiesApiVersion.COMMODITIES_XI_V2;
    return this.tradeTariffApi
        .getCommodity(
            commodityCode, dateOfTrade, commoditiesApiVersion)
        .flatMap(
            response -> {
              if (response.resultFound() == null || !response.resultFound()) {
                return Mono.error(
                    new ResourceNotFoundException(
                        "Commodity", commodityCode));
              }
              return Mono.just(response);
            });
  }
}
