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

import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingStep.ALL_COUNTRY_RESTRICTION;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingStep.EU_COUNTRY_RESTRICTION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.AppConfig;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingStep;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingStepHeader;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingSuperHeader;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.SignpostingStepRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.SignpostingSuperHeadersContentRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Header;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.RestrictiveMeasure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.RestrictiveMeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingContent;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingHeaderContent;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ConditionBasedRestrictiveMeasure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingStepResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SuperHeader;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.CommodityMeasuresRequest;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class SignpostingContentService {

  private static final String IMPORT_CONTROLS_RELATED_TO = "IMPORT_CONTROLS";

  private final SignpostingStepRepository signpostingStepRepository;
  private final MeasureTypeService measureTypeService;
  private final SignpostingSuperHeadersContentRepository signpostingSuperHeadersContentRepository;

  public Mono<List<SignpostingContent>> getSignpostingContents(
      CommodityMeasuresRequest request,
      TradeTariffCommodityResponse commodityResponse,
      List<Measure> measures) {

    Set<String> originRestrictions =
        CountryHelper.isEUCountry(request.getOriginCountry())
            ? Set.of(request.getOriginCountry(), EU_COUNTRY_RESTRICTION, ALL_COUNTRY_RESTRICTION)
            : Set.of(request.getOriginCountry(), ALL_COUNTRY_RESTRICTION);

    Flux<SignpostingStep> signpostingContentsByTradeType =
        signpostingStepRepository
            .findByTradeType(request.getTradeType(), request.getUserType(), Locale.EN)
            .name("dbFindByTradeType")
            .metrics()
            .filter(onOriginCountryRestrictions(originRestrictions))
            .filter(onDestinationCountryRestrictions(request));

    Flux<SignpostingStep> signpostingContentsByCommodityHierarchyCodes =
        signpostingStepRepository
            .findByTradeTypeAndCommodityHierarchyCodes(
                request.getTradeType(),
                extractHierarchyCodes(commodityResponse),
                request.getUserType(),
                Locale.EN)
            .name("dbFindByTradeTypeAndCommodityHierarchyCodes")
            .metrics()
            .filter(onOriginCountryRestrictions(originRestrictions))
            .filter(onDestinationCountryRestrictions(request));

    Flux<SignpostingStep> signpostingContentsBySection =
        signpostingStepRepository
            .findByTradeTypeAndSection(
                request.getTradeType(),
                Integer.valueOf(commodityResponse.getSection().getId()),
                request.getUserType(),
                Locale.EN)
            .name("dbFindByTradeTypeAndSection")
            .metrics()
            .filter(onOriginCountryRestrictions(originRestrictions))
            .filter(onDestinationCountryRestrictions(request));

    Flux<SignpostingStep> signpostingContentsByChapter =
        signpostingStepRepository
            .findByTradeTypeAndChapter(
                request.getTradeType(),
                getChapterId(commodityResponse),
                request.getUserType(),
                Locale.EN)
            .name("dbFindByTradeTypeAndChapter")
            .metrics()
            .filter(onOriginCountryRestrictions(originRestrictions))
            .filter(onDestinationCountryRestrictions(request));

    Flux<RestrictiveMeasure> measureTypeWithOptionsResponses =
        this.measureTypeService
            .getSignpostingMeasureTypeContents(
                measures, request.getCommodityCode(), request.getDestinationCountry())
            .name("getSignpostingMeasureTypeContents")
            .metrics();

    final Flux<SignpostingSuperHeader> dbFindAllByUserTypeAndLocale =
        signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
            request.getUserType(), AppConfig.LOCALE);
    return Mono.zip(
            dbFindAllByUserTypeAndLocale.collectList(),
            Flux.merge(
                    signpostingContentsByTradeType,
                    signpostingContentsByCommodityHierarchyCodes,
                    signpostingContentsBySection,
                    signpostingContentsByChapter)
                .distinct()
                .collectList(),
            measureTypeWithOptionsResponses.collectList())
        .map(
            allHeadersWithStepsAndWithComplexMeasures -> {
              Map<Integer, List<SignpostingStepHeader>> superHeaderToHeaderMapping =
                  allHeadersWithStepsAndWithComplexMeasures.getT1().stream()
                      .collect(
                          Collectors.groupingBy(
                              SignpostingSuperHeader::getId,
                              Collectors.mapping(
                                  SignpostingSuperHeader::getHeader, Collectors.toList())));

              Set<SignpostingSuperHeader> superHeaders =
                  allHeadersWithStepsAndWithComplexMeasures.getT1().stream()
                      .map(
                          s ->
                              SignpostingSuperHeader.builder()
                                  .id(s.getId())
                                  .description(s.getDescription())
                                  .explanatoryText(s.getExplanatoryText())
                                  .orderIndex(s.getOrderIndex())
                                  .build())
                      .collect(Collectors.toSet());
              Map<SignpostingStepHeader, List<SignpostingStep>> stepsByHeader =
                  allHeadersWithStepsAndWithComplexMeasures.getT2().stream()
                      .collect(Collectors.groupingBy(SignpostingStep::getHeader));
              return superHeaders.stream()
                  .map(
                      superHeader ->
                          SignpostingContent.builder()
                              .superHeader(
                                  SuperHeader.builder()
                                      .orderIndex(superHeader.getOrderIndex())
                                      .description(superHeader.getDescription())
                                      .explanatoryText(superHeader.getExplanatoryText())
                                      .build())
                              .headers(
                                  getSignpostingStepResponseList(
                                      superHeaderToHeaderMapping.get(superHeader.getId()),
                                      stepsByHeader,
                                      allHeadersWithStepsAndWithComplexMeasures.getT3()))
                              .build())
                  .sorted(
                      Comparator.comparingInt(
                          signpostingContent ->
                              signpostingContent.getSuperHeader().getOrderIndex()))
                  .collect(Collectors.toList());
            })
        .doOnSuccess(steps -> log.debug("Steps retrieved from DB : {}", steps))
        .name("getSignpostingContents")
        .metrics();
  }

  private Predicate<SignpostingStep> onDestinationCountryRestrictions(
      CommodityMeasuresRequest request) {
    return signpostingStep ->
        signpostingStep
            .getDestinationCountryRestrictions()
            .contains(request.getDestinationCountry());
  }

  private Predicate<SignpostingStep> onOriginCountryRestrictions(Set<String> originRestrictions) {
    return signpostingStep ->
        !Collections.disjoint(originRestrictions, signpostingStep.getOriginCountryRestrictions());
  }

  private Integer getChapterId(TradeTariffCommodityResponse commodityResponse) {
    return Integer.valueOf(
        commodityResponse.getChapter().getGoodsNomenclatureItemId().substring(0, 2));
  }

  private List<ConditionBasedRestrictiveMeasure> addComplexMeasures(
      List<RestrictiveMeasure> measureTypeContents, SignpostingStepHeader header) {
    return IMPORT_CONTROLS_RELATED_TO.equals(header.getRelatedEntityType())
        ? measureTypeContents.stream().filter(restrictiveMeasure -> restrictiveMeasure.getMeasureType() == RestrictiveMeasureType.RESTRICTIVE).map(ConditionBasedRestrictiveMeasure.class::cast).collect(
        Collectors.toList())
        : Collections.emptyList();
  }

  private List<String> extractHierarchyCodes(TradeTariffCommodityResponse commodityResponse) {
    final List<String> hierarchicalCommodityCodes = new ArrayList<>();
    hierarchicalCommodityCodes.add(commodityResponse.getData().getGoodsNomenclatureItemId());
    var optionalHeading = commodityResponse.getHeading();
    optionalHeading.ifPresent(
        commodityHeading ->
            hierarchicalCommodityCodes.add(commodityHeading.getGoodsNomenclatureItemId()));
    commodityResponse
        .getIncludedCommodities()
        .forEach(
            commodity -> hierarchicalCommodityCodes.add(commodity.getGoodsNomenclatureItemId()));
    return hierarchicalCommodityCodes;
  }

  private List<SignpostingHeaderContent> getSignpostingStepResponseList(
      List<SignpostingStepHeader> signpostingStepHeaderList,
      Map<SignpostingStepHeader, List<SignpostingStep>> headerContents,
      List<RestrictiveMeasure> measureTypeWithOptions) {
    if (CollectionUtils.isEmpty(signpostingStepHeaderList)) {
      return Collections.emptyList();
    }
    return signpostingStepHeaderList.stream()
        .map(
            header ->
                SignpostingHeaderContent.builder()
                    .header(
                        Header.builder()
                            .id(header.getId())
                            .orderIndex(header.getOrderIndex())
                            .description(header.getDescription())
                            .explanatoryText(header.getExplanatoryText())
                            .linkText(header.getLinkText())
                            .relatedTo(header.getRelatedEntityType())
                            .externalLink(header.getExternalLink())
                            .build())
                    .steps(getSignpostingStepResponseList(headerContents.get(header)))
                    .measures(addComplexMeasures(measureTypeWithOptions, header))
                    .build())
        .sorted(
            Comparator.comparingInt(
                signpostingContent -> signpostingContent.getHeader().getOrderIndex()))
        .collect(Collectors.toList());
  }

  private List<SignpostingStepResponse> getSignpostingStepResponseList(
      List<SignpostingStep> steps) {
    if (CollectionUtils.isEmpty(steps)) {
      return Collections.emptyList();
    }
    return steps.stream()
        .map(
            step ->
                SignpostingStepResponse.builder()
                    .id(step.getId())
                    .stepDescription(step.getStepDescription())
                    .stepHowtoDescription(step.getStepHowtoDescription())
                    .nonDeclaringTraderContent(step.getNonDeclaringTraderContent())
                    .declaringTraderContent(step.getDeclaringTraderContent())
                    .agentContent(step.getAgentContent())
                    .stepUrl(step.getStepUrl())
                    .build())
        .collect(Collectors.toList());
  }
}
