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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.CommodityHierarchyItem;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.CommodityMeasures;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Prohibition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingContent;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TaxAndDuty;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.service.helper.MeasureBuilder;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.CommodityMeasuresRequest;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class CommodityService {

  private final TradeTariffApiGateway tradeTariffApiGateway;
  private final MeasureFilterer measureFilterer;
  private final MeasureBuilder measureBuilder;
  private final SignpostingContentService signpostingContentService;
  private final ProhibitionContentService prohibitionContentService;
  private final TaxApplicabilityService taxApplicabilityService;

  public Mono<CommodityMeasures> getCommodityMeasures(
      CommodityMeasuresRequest commodityMeasuresRequest) {
    return this.tradeTariffApiGateway
        .getCommodity(
            commodityMeasuresRequest.getCommodityCode(),
            commodityMeasuresRequest.getImportDate(),
            commodityMeasuresRequest.getDestinationCountry())
        .flatMap(response -> toCommodityMeasuresResponse(response, commodityMeasuresRequest));
  }

  private Mono<CommodityMeasures> toCommodityMeasuresResponse(
      TradeTariffCommodityResponse tradeTariffCommodityResponse,
      CommodityMeasuresRequest commodityMeasuresRequest) {
    List<Measure> restrictiveMeasures =
        measureFilterer.getRestrictiveMeasures(
            measureBuilder.from(tradeTariffCommodityResponse), commodityMeasuresRequest.getTradeType(), commodityMeasuresRequest.getOriginCountry());
    List<Measure> restrictiveMeasuresWithAdditionalCodeFiltering =
        measureFilterer.maybeFilterByAdditionalCode(
            restrictiveMeasures, commodityMeasuresRequest.getAdditionalCode());
    Mono<List<Prohibition>> prohibitionListMono =
        prohibitionContentService.getProhibitions(
            restrictiveMeasuresWithAdditionalCodeFiltering,
            commodityMeasuresRequest.getOriginCountry(),
            Locale.EN,
            commodityMeasuresRequest.getTradeType());

    log.debug(
        String.format(
            "Calling DB to retrieve list of steps for request %s", commodityMeasuresRequest));
    Mono<List<SignpostingContent>> signpostingContentsMono =
        signpostingContentService.getSignpostingContents(
            commodityMeasuresRequest,
            tradeTariffCommodityResponse,
            restrictiveMeasuresWithAdditionalCodeFiltering);

    return Mono.zip(
            signpostingContentsMono,
            prohibitionListMono,
            Mono.defer(
                () ->
                    taxApplicabilityService.isApplicable(
                        commodityMeasuresRequest, tradeTariffCommodityResponse)))
        .map(
            tuple3 -> {
              log.debug("tuple3.getT2(): {}", tuple3.getT2());
              return CommodityMeasures.builder()
                  .commodityCode(
                      Optional.ofNullable(tradeTariffCommodityResponse.getData())
                          .map(TradeTariffCommodityResponseData::getGoodsNomenclatureItemId)
                          .orElse(""))
                  .commodityDescription(
                      Optional.ofNullable(tradeTariffCommodityResponse.getData())
                          .map(TradeTariffCommodityResponseData::getFormattedDescription)
                          .orElse(""))
                  .measures(restrictiveMeasuresWithAdditionalCodeFiltering)
                  .prohibitions(tuple3.getT2())
                  .signpostingContents(tuple3.getT1())
                  .commodityHierarchy(getCommodityHierarchy(tradeTariffCommodityResponse))
                  .taxAndDuty(TaxAndDuty.builder().applicable(tuple3.getT3()).build())
                  .build();
            });
  }

  private List<CommodityHierarchyItem> getCommodityHierarchy(
      TradeTariffCommodityResponse tradeTariffCommodityResponse) {
    List<CommodityHierarchyItem> commodityHierarchyEntities = new ArrayList<>();

    commodityHierarchyEntities.add(
        new CommodityHierarchyItem(tradeTariffCommodityResponse.getSection()));
    commodityHierarchyEntities.add(
        new CommodityHierarchyItem(tradeTariffCommodityResponse.getChapter()));
    if (tradeTariffCommodityResponse.getHeading().isPresent()) {
      commodityHierarchyEntities.add(
          new CommodityHierarchyItem(tradeTariffCommodityResponse.getHeading().get()));
    }
    commodityHierarchyEntities.addAll(
        tradeTariffCommodityResponse.getIncludedCommodities().stream()
            .map(CommodityHierarchyItem::new)
            .collect(Collectors.toList()));

    return commodityHierarchyEntities;
  }
}
