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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Prohibition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.RestrictiveMeasure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.helper.MeasureBuilder;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.MeasuresRequest;

@Service
@Slf4j
@AllArgsConstructor
public class MeasuresService {

  private final TradeTariffApiGateway tradeTariffApiGateway;
  private final MeasureFilterer measureFilterer;
  private final MeasureBuilder measureBuilder;
  private final MeasureTypeService measureTypeService;
  private final ProhibitionContentService prohibitionContentService;

  public Flux<RestrictiveMeasure> getMeasures(MeasuresRequest measuresRequest) {
    return this.tradeTariffApiGateway
        .getCommodity(
            measuresRequest.getCommodityCode(),
            measuresRequest.getDateOfTrade(),
            measuresRequest.getTradeType() == TradeType.IMPORT
                ? UkCountry.valueOf(measuresRequest.getDestinationCountry())
                : UkCountry.valueOf(measuresRequest.getOriginCountry()))
        .flatMapMany(
            response ->
                toMeasuresResponse(
                    response,
                    measuresRequest,
                    measuresRequest.getCommodityCode(),
                    measuresRequest.getLocale()));
  }

  private Flux<RestrictiveMeasure> toMeasuresResponse(
      TradeTariffCommodityResponse tradeTariffCommodityResponse,
      MeasuresRequest measuresRequest,
      String commodityCode,
      Locale locale) {
    List<Measure> restrictiveMeasures =
        measureFilterer.getRestrictiveMeasures(
            measureBuilder.from(tradeTariffCommodityResponse, commodityCode),
            measuresRequest.getTradeType(),
            measuresRequest.getTradeType() == TradeType.IMPORT
                ? measuresRequest.getOriginCountry()
                : measuresRequest.getDestinationCountry());
    List<Measure> restrictiveMeasuresWithAdditionalCodeFiltering =
        measureFilterer.maybeFilterByAdditionalCode(
            restrictiveMeasures, measuresRequest.getAdditionalCode());

    Mono<List<Prohibition>> prohibitionListMono =
        prohibitionContentService.getProhibitions(
            restrictiveMeasuresWithAdditionalCodeFiltering,
            measuresRequest.getTradeType() == TradeType.IMPORT
                ? measuresRequest.getOriginCountry()
                : measuresRequest.getDestinationCountry(),
            locale,
            measuresRequest.getTradeType());

    return this.measureTypeService
        .getRestrictiveMeasures(
            restrictiveMeasuresWithAdditionalCodeFiltering,
            measuresRequest.getCommodityCode(),
            measuresRequest.getTradeType(),
            measuresRequest.getLocale())
        .concatWith(prohibitionListMono.flatMapMany(Flux::fromIterable));
  }
}
