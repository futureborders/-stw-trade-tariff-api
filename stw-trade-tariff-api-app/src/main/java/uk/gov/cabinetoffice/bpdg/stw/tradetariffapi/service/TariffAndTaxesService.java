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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TariffAndTaxes;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.helper.MeasureBuilder;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.TariffAndTaxesRequest;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class TariffAndTaxesService {

  private final TradeTariffApiGateway tradeTariffApiGateway;
  private final MeasureFilterer measureFilterer;
  private final MeasureBuilder measureBuilder;
  private final DutyMeasureService dutyMeasureService;

  public Mono<TariffAndTaxes> getTariffAndTaxes(TariffAndTaxesRequest tariffAndTaxesRequest) {

    if (tariffAndTaxesRequest.getDestinationCountry() == UkCountry.XI) {
      return Mono.just(TariffAndTaxes.builder().duties(List.of()).build());
    }
    return this.tradeTariffApiGateway
        .getCommodity(
            tariffAndTaxesRequest.getCommodityCode(),
            tariffAndTaxesRequest.getImportDate(),
            tariffAndTaxesRequest.getDestinationCountry())
        .flatMap(response -> handleMeasures(response, tariffAndTaxesRequest));
  }

  private Mono<TariffAndTaxes> handleMeasures(
      TradeTariffCommodityResponse tradeTariffCommodityResponse,
      TariffAndTaxesRequest tariffAndTaxesRequest) {
    List<Measure> dutyMeasures =
        measureFilterer.getTaxAndDutyMeasures(
            measureBuilder.from(
                tradeTariffCommodityResponse, tariffAndTaxesRequest.getCommodityCode()),
            tariffAndTaxesRequest.getTradeType(),
            tariffAndTaxesRequest.getOriginCountry());

    return this.dutyMeasureService
        .getTariffsAndTaxesMeasures(
            dutyMeasures, tariffAndTaxesRequest.getTradeType(), tariffAndTaxesRequest.getLocale())
        .collectList()
        .map(duties -> TariffAndTaxes.builder().duties(duties).build());
  }
}
