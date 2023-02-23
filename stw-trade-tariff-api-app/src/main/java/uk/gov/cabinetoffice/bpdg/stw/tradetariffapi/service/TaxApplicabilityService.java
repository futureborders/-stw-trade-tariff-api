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

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.CommodityMeasuresRequest;

@Service
public class TaxApplicabilityService {

  public Mono<Boolean> isApplicable(
      CommodityMeasuresRequest commodityMeasuresRequest,
      TradeTariffCommodityResponse tradeTariffCommodityResponse) {
    if (commodityMeasuresRequest.getDestinationCountry() == UkCountry.GB) {
      if (!commodityMeasuresRequest.getOriginCountry().equals("XI")
          && (tradeTariffCommodityResponse.getData().getTaxAndDuty().hasTradeRemedies()
              || tradeTariffCommodityResponse
                  .getData()
                  .getTaxAndDuty()
                  .hasMostFavouredNationDuty())) {
        return Mono.just(Boolean.TRUE);
      } else {
        return Mono.just(Boolean.FALSE);
      }
    } else if (commodityMeasuresRequest.getDestinationCountry() == UkCountry.XI){
      if (CountryHelper.isEUCountry(commodityMeasuresRequest.getOriginCountry()) || (commodityMeasuresRequest.getOriginCountry().equals("GB") && !tradeTariffCommodityResponse.getData().getTaxAndDuty().hasTradeRemedies()
          && !tradeTariffCommodityResponse
          .getData()
          .getTaxAndDuty()
          .hasMostFavouredNationDuty())){
        return Mono.just(Boolean.FALSE);
      } else {
        return Mono.just(Boolean.TRUE);
      }
    }
    throw new IllegalStateException(format("Not a recognised destination country '%s'", commodityMeasuresRequest.getDestinationCountry()));
  }
}
