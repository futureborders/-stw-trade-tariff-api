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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import reactor.core.publisher.Flux;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponse;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.TradeTariffCommodityResponseData;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityChapter;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityHeading;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityImpl;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommoditySection;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.TradeTariffCommodityResponseIncludedEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.SignpostingContentDbConfig;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.constants.CacheConstants;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingStep;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingStepHeader;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingSuperHeader;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingSuperHeader.SignpostingSuperHeaderBuilder;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.SignpostingStepRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.SignpostingSuperHeadersContentRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Header;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Prohibition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingContent;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ConditionBasedRestrictiveMeasure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingStepResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SuperHeader;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UserType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.CommodityMeasuresRequest;

@ExtendWith(MockitoExtension.class)
public class SignpostingContentServiceTest {

  private static final String COMMODITY_CODE = "2007109995";
  private static final String HEADING_CODE = "100";
  private static final Integer SECTION_ID = 1;
  private static final Integer CHAPTER_ID = 10;
  private static final String CHAPTER_GOODS_NOMENCLATURE_ITEM_ID = "1000000000";

  @Mock private SignpostingStepRepository signpostingStepRepository;
  @Mock private MeasureTypeService measureTypeService;
  @Mock private SignpostingSuperHeadersContentRepository signpostingSuperHeadersContentRepository;

  private SignpostingContentService signpostingContentService;

  @BeforeEach
  public void init() {
    ApplicationProperties applicationProperties =
      ApplicationProperties.builder()
        .signpostingContentDb(
          SignpostingContentDbConfig.builder().timeout(Duration.ofMillis(500)).build())
        .build();

    assertThat(applicationProperties).isNotNull();

    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(
      Collections.singletonList(new ConcurrentMapCache(CacheConstants.SUPER_HEADERS_CACHE)));
    cacheManager.initializeCaches();
    signpostingContentService =
      new SignpostingContentService(
        signpostingStepRepository,
        measureTypeService,
        signpostingSuperHeadersContentRepository);
  }

  @Nested
  class GetSignpostingContents {

    @Test
    @DisplayName("happy path")
    void happyPath() {
      final SignpostingStepHeader signpostingStepHeader =
        SignpostingStepHeader.builder()
          .id(1)
          .relatedEntityType(null)
          .orderIndex(1)
          .description("Get your business ready")
          .linkText("Register to bring goods across border")
          .build();
      final SignpostingStepHeader signpostingStepHeaderWithComplexMeasures =
        SignpostingStepHeader.builder()
          .id(4)
          .relatedEntityType("IMPORT_CONTROLS")
          .orderIndex(2)
          .description("Check if you need an import license or certificate")
          .linkText("Licences certificates restrictions")
          .build();
      final Header expectedHeaderWithStepsAndWithoutComplexMeasures =
        Header.builder()
          .id(signpostingStepHeader.getId())
          .orderIndex(signpostingStepHeader.getOrderIndex())
          .description(signpostingStepHeader.getDescription())
          .linkText(signpostingStepHeader.getLinkText())
          .build();

      final Header expectedHeaderWithoutStepAndWithComplexMeasures =
        Header.builder()
          .id(signpostingStepHeaderWithComplexMeasures.getId())
          .orderIndex(signpostingStepHeaderWithComplexMeasures.getOrderIndex())
          .description(signpostingStepHeaderWithComplexMeasures.getDescription())
          .linkText(signpostingStepHeaderWithComplexMeasures.getLinkText())
          .relatedTo("IMPORT_CONTROLS")
          .build();

      UkCountry destinationCountryGB = UkCountry.GB;

      CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode("2804501000")
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .destinationCountry(destinationCountryGB)
          .originCountry("FR")
          .build();

      Measure measureWithoutDocumentCode =
        Measure.builder()
          .measureType(MeasureType.builder().id("VTZ").description("VAT zero rate").build())
          .measureConditions(List.of(MeasureCondition.builder().build()))
          .build();

      Measure measureWithMeasureConditions =
        Measure.builder()
          .measureType(
            MeasureType.builder()
              .id("750")
              .description("Import control of organic products")
              .build())
          .measureConditions(List.of(MeasureCondition.builder().documentCode("C644").build()))
          .build();
      List<Measure> measureList = List.of(measureWithoutDocumentCode, measureWithMeasureConditions);

      SignpostingStep signpostingStepByTradeTypeForGB =
        SignpostingStep.builder()
          .id(1)
          .stepDescription("Content assigned to any commodity for IMPORT")
          .stepHowtoDescription("HowToDescription_1")
          .agentContent("agentContent_1")
          .declaringTraderContent("declaringTraderContent_1")
          .nonDeclaringTraderContent("nonDeclaringTraderContent_1")
          .stepUrl("http://steptest.url.1")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(destinationCountryGB))
          .build();

      SignpostingStep signpostingStepByTradeTypeForXI =
        SignpostingStep.builder()
          .id(21)
          .stepDescription("Description_21")
          .stepHowtoDescription("HowToDescription_21")
          .agentContent("agentContent_21")
          .declaringTraderContent("declaringTraderContent_21")
          .nonDeclaringTraderContent("nonDeclaringTraderContent_21")
          .stepUrl("http://steptest.url.21")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(UkCountry.XI))
          .build();

      SignpostingStep signpostingStepByCommodityCodeForGB =
        SignpostingStep.builder()
          .id(4)
          .stepDescription("Content assigned to commodity code")
          .stepHowtoDescription("HowToDescription_4")
          .agentContent("agentContent_4")
          .declaringTraderContent("declaringTraderContent_4")
          .nonDeclaringTraderContent("nonDeclaringTraderContent_4")
          .stepUrl("http://steptest.url.4")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(destinationCountryGB))
          .build();

      SignpostingStep signpostingStepByCommodityCodeForXI =
        SignpostingStep.builder()
          .id(22)
          .stepDescription("Content assigned to commodity code")
          .stepHowtoDescription("HowToDescription_22")
          .agentContent("agentContent_22")
          .declaringTraderContent("declaringTraderContent_22")
          .nonDeclaringTraderContent("nonDeclaringTraderContent_22")
          .stepUrl("http://steptest.url.22")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(UkCountry.XI))
          .build();

      SignpostingStep signpostingStepBySectionForGB =
        SignpostingStep.builder()
          .id(5)
          .stepDescription("Content assigned to section")
          .stepHowtoDescription("HowToDescription_5")
          .agentContent("agentContent_5")
          .declaringTraderContent("declaringTraderContent_5")
          .nonDeclaringTraderContent("nonDeclaringTraderContent_5")
          .stepUrl("http://steptest.url.5")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(destinationCountryGB))
          .build();

      SignpostingStep signpostingStepBySectionForXI =
        SignpostingStep.builder()
          .id(23)
          .stepDescription("Description_23")
          .stepHowtoDescription("HowToDescription_23")
          .agentContent("agentContent_23")
          .declaringTraderContent("declaringTraderContent_23")
          .nonDeclaringTraderContent("nonDeclaringTraderContent_23")
          .stepUrl("http://steptest.url.23")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(UkCountry.XI))
          .build();

      SignpostingStep signpostingStepByChapterForGB =
        SignpostingStep.builder()
          .id(6)
          .stepDescription("Content assigned to chapter")
          .stepHowtoDescription("HowToDescription_6")
          .agentContent("agentContent_6")
          .declaringTraderContent("declaringTraderContent_6")
          .nonDeclaringTraderContent("nonDeclaringTraderContent_6")
          .stepUrl("http://steptest.url.6")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(destinationCountryGB))
          .build();

      SignpostingStep signpostingStepByChapterForXI =
        SignpostingStep.builder()
          .id(24)
          .stepDescription("Description_24")
          .stepHowtoDescription("HowToDescription_24")
          .agentContent("agentContent_24")
          .declaringTraderContent("declaringTraderContent_24")
          .nonDeclaringTraderContent("nonDeclaringTraderContent_24")
          .stepUrl("http://steptest.url.24")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(UkCountry.XI))
          .build();
      String superHeaderDescription = "super header 1";
      int superHeaderId = 1;
      int superHeaderOrderIndex = 1;
      String superHeaderExplanatoryText = "super header explanatory text";
      SignpostingSuperHeaderBuilder superHeaderBuilder =
        SignpostingSuperHeader.builder()
          .id(superHeaderId)
          .orderIndex(superHeaderOrderIndex)
          .description(superHeaderDescription)
          .explanatoryText(superHeaderExplanatoryText);
      SignpostingSuperHeader superHeader = superHeaderBuilder.header(signpostingStepHeader).build();
      SignpostingSuperHeader superHeaderLinkedToComplexMeasures =
        superHeaderBuilder.header(signpostingStepHeaderWithComplexMeasures).build();

      when(signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
        UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.just(superHeader, superHeaderLinkedToComplexMeasures));

      when(signpostingStepRepository.findByTradeType(
        request.getTradeType(), UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.just(signpostingStepByTradeTypeForGB, signpostingStepByTradeTypeForXI));
      when(signpostingStepRepository.findByTradeTypeAndCommodityHierarchyCodes(
        request.getTradeType(),
        List.of(COMMODITY_CODE, HEADING_CODE),
        UserType.DECLARING_TRADER,
        Locale.EN))
        .thenReturn(
          Flux.just(signpostingStepByCommodityCodeForGB, signpostingStepByCommodityCodeForXI));
      when(signpostingStepRepository.findByTradeTypeAndSection(
        request.getTradeType(), SECTION_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.just(signpostingStepBySectionForGB, signpostingStepBySectionForXI));
      when(signpostingStepRepository.findByTradeTypeAndChapter(
        request.getTradeType(), CHAPTER_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.just(signpostingStepByChapterForGB, signpostingStepByChapterForXI));
      when(measureTypeService.getSignpostingMeasureTypeContents(
        measureList, request.getCommodityCode(), destinationCountryGB))
        .thenReturn(Flux.just(ConditionBasedRestrictiveMeasure.builder().build()));

      final List<TradeTariffCommodityResponseIncludedEntity> included =
        List.of(
          CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
          CommodityChapter.builder()
            .id(String.valueOf(CHAPTER_ID))
            .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
            .build(),
          CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(included)
          .build();

      List<SignpostingContent> signpostingContents =
        signpostingContentService
          .getSignpostingContents(request, tradeTariffCommodityResponse, measureList)
          .block();

      assertThat(signpostingContents).isNotNull().hasSize(1);

      SignpostingContent superHeaderContent = signpostingContents.get(0);
      assertThat(superHeaderContent.getSuperHeader().getDescription())
        .isEqualTo(superHeaderDescription);
      assertThat(superHeaderContent.getSuperHeader().getExplanatoryText())
        .isEqualTo(superHeaderExplanatoryText);
      assertThat(superHeaderContent.getSuperHeader().getOrderIndex())
        .isEqualTo(superHeaderOrderIndex);

      // check first header with steps
      assertThat(superHeaderContent.getHeaders().get(0).getHeader())
        .isEqualTo(expectedHeaderWithStepsAndWithoutComplexMeasures);
      assertThat(superHeaderContent.getHeaders().get(0).getSteps()).hasSize(4);

      List<SignpostingStepResponse> stepsResponse =
        superHeaderContent.getHeaders().get(0).getSteps();
      assertThat(stepsResponse)
        .containsExactlyInAnyOrder(
          SignpostingStepResponse.builder()
            .id(1)
            .stepDescription("Content assigned to any commodity for IMPORT")
            .stepDescription("Content assigned to any commodity for IMPORT")
            .stepHowtoDescription("HowToDescription_1")
            .agentContent("agentContent_1")
            .declaringTraderContent("declaringTraderContent_1")
            .nonDeclaringTraderContent("nonDeclaringTraderContent_1")
            .stepUrl("http://steptest.url.1")
            .build(),
          SignpostingStepResponse.builder()
            .id(4)
            .stepDescription("Content assigned to commodity code")
            .stepHowtoDescription("HowToDescription_4")
            .agentContent("agentContent_4")
            .declaringTraderContent("declaringTraderContent_4")
            .nonDeclaringTraderContent("nonDeclaringTraderContent_4")
            .stepUrl("http://steptest.url.4")
            .build(),
          SignpostingStepResponse.builder()
            .id(5)
            .stepDescription("Content assigned to section")
            .stepHowtoDescription("HowToDescription_5")
            .agentContent("agentContent_5")
            .declaringTraderContent("declaringTraderContent_5")
            .nonDeclaringTraderContent("nonDeclaringTraderContent_5")
            .stepUrl("http://steptest.url.5")
            .build(),
          SignpostingStepResponse.builder()
            .id(6)
            .stepDescription("Content assigned to chapter")
            .stepHowtoDescription("HowToDescription_6")
            .agentContent("agentContent_6")
            .declaringTraderContent("declaringTraderContent_6")
            .nonDeclaringTraderContent("nonDeclaringTraderContent_6")
            .stepUrl("http://steptest.url.6")
            .build());

      // check first header with complex measures
      assertThat(superHeaderContent.getHeaders().get(1).getHeader())
        .isEqualTo(expectedHeaderWithoutStepAndWithComplexMeasures);
      assertThat(superHeaderContent.getHeaders().get(1).getSteps()).isEmpty();
      assertThat(superHeaderContent.getHeaders().get(1).getMeasures()).isNotEmpty();
    }

    @Test
    @DisplayName("happy path with steps assigned only to trade type")
    void happyPath_only_trade_type() {
      UkCountry destinationCountryGB = UkCountry.GB;

      CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode("2804501000")
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .originCountry("FR")
          .destinationCountry(destinationCountryGB)
          .build();

      final SignpostingStepHeader signpostingStepHeader =
        SignpostingStepHeader.builder()
          .id(1)
          .relatedEntityType(null)
          .orderIndex(1)
          .description("Get your business ready")
          .build();

      final Header expectedHeader =
        Header.builder()
          .id(signpostingStepHeader.getId())
          .orderIndex(signpostingStepHeader.getOrderIndex())
          .description(signpostingStepHeader.getDescription())
          .build();

      List<SignpostingStep> signpostingStepsByTradeType =
        List.of(
          SignpostingStep.builder()
            .id(1)
            .stepDescription("Content assigned to any commodity for IMPORT")
            .header(signpostingStepHeader)
            .build(),
          SignpostingStep.builder()
            .id(2)
            .stepDescription("Content 2 assigned to any commodity for IMPORT")
            .header(signpostingStepHeader)
            .build());

      when(signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
        UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(
          Flux.just(
            SignpostingSuperHeader.builder()
              .id(1)
              .orderIndex(1)
              .header(signpostingStepHeader)
              .build()));

      when(signpostingStepRepository.findByTradeType(
        request.getTradeType(), UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.fromIterable(signpostingStepsByTradeType));

      when(signpostingStepRepository.findByTradeTypeAndCommodityHierarchyCodes(
        request.getTradeType(),
        List.of(COMMODITY_CODE, HEADING_CODE),
        UserType.DECLARING_TRADER,
        Locale.EN))
        .thenReturn(Flux.empty());

      when(signpostingStepRepository.findByTradeTypeAndSection(
        request.getTradeType(), SECTION_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.empty());

      when(signpostingStepRepository.findByTradeTypeAndChapter(
        request.getTradeType(), CHAPTER_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.empty());

      when(measureTypeService.getSignpostingMeasureTypeContents(
        List.of(), request.getCommodityCode(), destinationCountryGB))
        .thenReturn(Flux.empty());

      final List<TradeTariffCommodityResponseIncludedEntity> included =
        List.of(
          CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
          CommodityChapter.builder()
            .id(String.valueOf(CHAPTER_ID))
            .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
            .build(),
          CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(included)
          .build();

      List<SignpostingContent> signpostingContents =
        signpostingContentService
          .getSignpostingContents(request, tradeTariffCommodityResponse, List.of())
          .block();

      assertThat(signpostingContents).isNotNull().hasSize(1);
      SignpostingContent superHeaderContent = signpostingContents.get(0);
      assertThat(superHeaderContent.getSuperHeader()).isNotNull();
      assertThat(superHeaderContent.getHeaders()).hasSize(1);
      assertThat(superHeaderContent.getHeaders().get(0).getHeader()).isEqualTo(expectedHeader);
      assertThat(superHeaderContent.getHeaders().get(0).getSteps())
        .containsExactlyInAnyOrder(
          SignpostingStepResponse.builder()
            .id(1)
            .stepDescription("Content assigned to any commodity for IMPORT")
            .build(),
          SignpostingStepResponse.builder()
            .id(2)
            .stepDescription("Content 2 assigned to any commodity for IMPORT")
            .build());
    }

    @Test
    @DisplayName("restriction based on destination country")
    void signpostingStepsDefinedForNIButRequestedForGB() {
      UkCountry destinationCountryGB = UkCountry.GB;

      CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode("2804501000")
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .originCountry("DE")
          .destinationCountry(destinationCountryGB)
          .build();

      final SignpostingStepHeader signpostingStepHeader =
        SignpostingStepHeader.builder()
          .id(1)
          .relatedEntityType(null)
          .orderIndex(1)
          .description("Get your business ready")
          .build();

      final Header expectedHeader =
        Header.builder()
          .id(signpostingStepHeader.getId())
          .orderIndex(signpostingStepHeader.getOrderIndex())
          .description(signpostingStepHeader.getDescription())
          .build();

      List<SignpostingStep> signpostingStepsByTradeType =
        List.of(
          SignpostingStep.builder()
            .id(1)
            .stepDescription("Content assigned to any commodity for IMPORT")
            .header(signpostingStepHeader)
            .build(),
          SignpostingStep.builder()
            .id(2)
            .stepDescription("Content 2 assigned to any commodity for IMPORT")
            .header(signpostingStepHeader)
            .build());

      SignpostingStep signpostingStepByCommodityCodeForXI =
        SignpostingStep.builder()
          .id(4)
          .stepDescription("Content assigned to commodity code")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(UkCountry.XI))
          .build();

      SignpostingStep signpostingStepBySectionForXI =
        SignpostingStep.builder()
          .id(5)
          .stepDescription("Content assigned to section")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(UkCountry.XI))
          .build();

      SignpostingStep signpostingStepByChapterForXI =
        SignpostingStep.builder()
          .id(6)
          .stepDescription("Content assigned to chapter")
          .header(signpostingStepHeader)
          .destinationCountryRestrictions(Set.of(UkCountry.XI))
          .build();

      when(signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
        UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(
          Flux.just(
            SignpostingSuperHeader.builder()
              .id(1)
              .orderIndex(1)
              .header(signpostingStepHeader)
              .build()));

      when(signpostingStepRepository.findByTradeType(
        request.getTradeType(), UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.fromIterable(signpostingStepsByTradeType));

      when(signpostingStepRepository.findByTradeTypeAndCommodityHierarchyCodes(
        request.getTradeType(),
        List.of(COMMODITY_CODE, HEADING_CODE),
        UserType.DECLARING_TRADER,
        Locale.EN))
        .thenReturn(Flux.just(signpostingStepByCommodityCodeForXI));

      when(signpostingStepRepository.findByTradeTypeAndSection(
        request.getTradeType(), SECTION_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.just(signpostingStepBySectionForXI));

      when(signpostingStepRepository.findByTradeTypeAndChapter(
        request.getTradeType(), CHAPTER_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.just(signpostingStepByChapterForXI));

      when(measureTypeService.getSignpostingMeasureTypeContents(
        List.of(), request.getCommodityCode(), destinationCountryGB))
        .thenReturn(Flux.empty());

      final List<TradeTariffCommodityResponseIncludedEntity> included =
        List.of(
          CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
          CommodityChapter.builder()
            .id(String.valueOf(CHAPTER_ID))
            .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
            .build(),
          CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(included)
          .build();

      List<SignpostingContent> signpostingContents =
        signpostingContentService
          .getSignpostingContents(request, tradeTariffCommodityResponse, List.of())
          .block();

      assertThat(signpostingContents).isNotNull().hasSize(1);
      SignpostingContent superHeaderContent = signpostingContents.get(0);
      assertThat(superHeaderContent.getSuperHeader()).isNotNull();
      assertThat(superHeaderContent.getHeaders()).hasSize(1);
      assertThat(superHeaderContent.getHeaders().get(0).getHeader()).isEqualTo(expectedHeader);
      assertThat(superHeaderContent.getHeaders().get(0).getSteps())
        .containsExactlyInAnyOrder(
          SignpostingStepResponse.builder()
            .id(1)
            .stepDescription("Content assigned to any commodity for IMPORT")
            .build(),
          SignpostingStepResponse.builder()
            .id(2)
            .stepDescription("Content 2 assigned to any commodity for IMPORT")
            .build());
    }
  }

  @Nested
  class GetSignpostingContentsBasedOnOriginCountryRestrictions {

    @Test
    @DisplayName(
      "should return global signposting steps when steps are not configured for the country")
    void shouldReturnGlobalSignpostingStepsWhenStepsAreNotConfiguredForTheCountry() {
      final String originCountry = "AU";
      final UkCountry destinationCountryGB = UkCountry.GB;

      final CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode("2804501000")
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .destinationCountry(destinationCountryGB)
          .originCountry(originCountry)
          .build();

      List<Measure> measureList = List.of();

      setUpMockedResponses(destinationCountryGB, request, measureList);

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(
            List.of(
              CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
              CommodityChapter.builder()
                .id(String.valueOf(CHAPTER_ID))
                .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
                .build(),
              CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build()))
          .build();

      List<SignpostingContent> signpostingContents =
        signpostingContentService
          .getSignpostingContents(request, tradeTariffCommodityResponse, measureList)
          .block();

      assertThat(signpostingContents).isNotNull().hasSize(1);
      assertThat(signpostingContents.get(0).getHeaders()).hasSize(1);
      assertThat(signpostingContents.get(0).getHeaders().get(0).getSteps())
        .containsExactlyInAnyOrder(
          SignpostingStepResponse.builder()
            .id(4)
            .stepDescription(
              "Content assigned to any commodity for IMPORT from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(8)
            .stepDescription("Content assigned to commodity from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(12)
            .stepDescription("Content assigned to section from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(16)
            .stepDescription("Content assigned to chapter from all countries")
            .build());
    }

    @Test
    @DisplayName("should return EU and global signposting steps when origin country is part of EU")
    void shouldReturnEUAndGlobalSignpostingStepsWhenOriginCountryIsPartOfEU() {
      final String originCountry = "DE";
      final UkCountry destinationCountryGB = UkCountry.GB;

      final CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode("2804501000")
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .destinationCountry(destinationCountryGB)
          .originCountry(originCountry)
          .build();

      List<Measure> measureList = List.of();

      setUpMockedResponses(destinationCountryGB, request, measureList);

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(
            List.of(
              CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
              CommodityChapter.builder()
                .id(String.valueOf(CHAPTER_ID))
                .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
                .build(),
              CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build()))
          .build();

      List<SignpostingContent> signpostingContents =
        signpostingContentService
          .getSignpostingContents(request, tradeTariffCommodityResponse, measureList)
          .block();

      assertThat(signpostingContents).isNotNull().hasSize(1);
      assertThat(signpostingContents.get(0).getHeaders().get(0).getSteps())
        .containsExactlyInAnyOrder(
          SignpostingStepResponse.builder()
            .id(1)
            .stepDescription("Content assigned to any commodity for IMPORT from EU")
            .build(),
          SignpostingStepResponse.builder()
            .id(4)
            .stepDescription(
              "Content assigned to any commodity for IMPORT from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(5)
            .stepDescription("Content assigned to commodity from EU")
            .build(),
          SignpostingStepResponse.builder()
            .id(8)
            .stepDescription("Content assigned to commodity from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(9)
            .stepDescription("Content assigned to section from EU")
            .build(),
          SignpostingStepResponse.builder()
            .id(12)
            .stepDescription("Content assigned to section from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(13)
            .stepDescription("Content assigned to chapter from EU")
            .build(),
          SignpostingStepResponse.builder()
            .id(16)
            .stepDescription("Content assigned to chapter from all countries")
            .build());
    }

    @Test
    @DisplayName(
      "should return steps configured for that country, EU and global signposting steps when origin country is part of EU")
    void
    shouldReturnCountrySpecificStepsAlongWithEUAndGlobalSignpostingStepsWhenOriginCountryIsPartOfEU() {
      final String originCountry = "FR";
      final UkCountry destinationCountryGB = UkCountry.GB;

      final CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode("2804501000")
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .destinationCountry(destinationCountryGB)
          .originCountry(originCountry)
          .build();

      List<Measure> measureList = List.of();

      setUpMockedResponses(destinationCountryGB, request, measureList);

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(
            List.of(
              CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
              CommodityChapter.builder()
                .id(String.valueOf(CHAPTER_ID))
                .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
                .build(),
              CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build()))
          .build();

      List<SignpostingContent> signpostingContents =
        signpostingContentService
          .getSignpostingContents(request, tradeTariffCommodityResponse, measureList)
          .block();

      assertThat(signpostingContents).isNotNull().hasSize(1);
      assertThat(signpostingContents.get(0).getHeaders()).hasSize(1);
      assertThat(signpostingContents.get(0).getHeaders().get(0).getSteps())
        .containsExactlyInAnyOrder(
          SignpostingStepResponse.builder()
            .id(1)
            .stepDescription("Content assigned to any commodity for IMPORT from EU")
            .build(),
          SignpostingStepResponse.builder()
            .id(2)
            .stepDescription("Content assigned to any commodity for IMPORT from FR")
            .build(),
          SignpostingStepResponse.builder()
            .id(4)
            .stepDescription(
              "Content assigned to any commodity for IMPORT from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(5)
            .stepDescription("Content assigned to commodity from EU")
            .build(),
          SignpostingStepResponse.builder()
            .id(6)
            .stepDescription("Content assigned to commodity from FR")
            .build(),
          SignpostingStepResponse.builder()
            .id(8)
            .stepDescription("Content assigned to commodity from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(9)
            .stepDescription("Content assigned to section from EU")
            .build(),
          SignpostingStepResponse.builder()
            .id(10)
            .stepDescription("Content assigned to section from FR")
            .build(),
          SignpostingStepResponse.builder()
            .id(12)
            .stepDescription("Content assigned to section from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(13)
            .stepDescription("Content assigned to chapter from EU")
            .build(),
          SignpostingStepResponse.builder()
            .id(14)
            .stepDescription("Content assigned to chapter from FR")
            .build(),
          SignpostingStepResponse.builder()
            .id(16)
            .stepDescription("Content assigned to chapter from all countries")
            .build());
    }

    @Test
    @DisplayName(
      "should return steps configured for that country and global when origin country is not part of EU")
    void shouldReturnCountrySpecificStepsAlongWithGlobalStepsWhenOriginCountryIsNotPartOfEU() {
      final String originCountry = "IN";
      final UkCountry destinationCountryGB = UkCountry.GB;

      final CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode("2804501000")
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .destinationCountry(destinationCountryGB)
          .originCountry(originCountry)
          .build();

      List<Measure> measureList = List.of();

      setUpMockedResponses(destinationCountryGB, request, measureList);

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(
            List.of(
              CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
              CommodityChapter.builder()
                .id(String.valueOf(CHAPTER_ID))
                .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
                .build(),
              CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build()))
          .build();

      List<SignpostingContent> signpostingContents =
        signpostingContentService
          .getSignpostingContents(request, tradeTariffCommodityResponse, measureList)
          .block();

      assertThat(signpostingContents).isNotNull().hasSize(1);
      assertThat(signpostingContents.get(0).getHeaders()).hasSize(1);
      assertThat(signpostingContents.get(0).getHeaders().get(0).getSteps())
        .containsExactlyInAnyOrder(
          SignpostingStepResponse.builder()
            .id(3)
            .stepDescription("Content assigned to any commodity for IMPORT from IN")
            .build(),
          SignpostingStepResponse.builder()
            .id(4)
            .stepDescription(
              "Content assigned to any commodity for IMPORT from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(7)
            .stepDescription("Content assigned to commodity from IN")
            .build(),
          SignpostingStepResponse.builder()
            .id(8)
            .stepDescription("Content assigned to commodity from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(11)
            .stepDescription("Content assigned to section from IN")
            .build(),
          SignpostingStepResponse.builder()
            .id(12)
            .stepDescription("Content assigned to section from all countries")
            .build(),
          SignpostingStepResponse.builder()
            .id(15)
            .stepDescription("Content assigned to chapter from IN")
            .build(),
          SignpostingStepResponse.builder()
            .id(16)
            .stepDescription("Content assigned to chapter from all countries")
            .build());
    }

    @Test
    @DisplayName(
      "should return country specific signposting steps when steps are configured for more than one country")
    void shouldReturnCountrySpecificStepsWhenStepsAreConfiguredForMoreThanOneCountry() {
      final String originCountry = "AU";
      final UkCountry destinationCountryGB = UkCountry.GB;

      final CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode("2804501000")
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .destinationCountry(destinationCountryGB)
          .originCountry(originCountry)
          .build();

      List<Measure> measureList = List.of();

      final SignpostingStepHeader signpostingStepHeader =
        SignpostingStepHeader.builder()
          .id(1)
          .relatedEntityType(null)
          .orderIndex(1)
          .description("Get your business ready")
          .build();

      SignpostingStep signpostingStepByTradeTypeForINAndAU =
        SignpostingStep.builder()
          .id(1)
          .stepDescription("Content assigned to any commodity from IN and AU")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("IN", "AU"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndCommodityForINAndAU =
        SignpostingStep.builder()
          .id(2)
          .stepDescription("Content assigned to commodity for IN and AU")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("IN", "AU"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndSectionForINAndAU =
        SignpostingStep.builder()
          .id(3)
          .stepDescription("Content assigned to section for IN and AU")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("IN", "AU"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndChapterForINAndAU =
        SignpostingStep.builder()
          .id(4)
          .stepDescription("Content assigned to chapter for IN and AU")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("IN", "AU"))
          .build();

      when(signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
        UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(
          Flux.just(
            SignpostingSuperHeader.builder()
              .id(1)
              .orderIndex(1)
              .header(signpostingStepHeader)
              .build()));
      when(signpostingStepRepository.findByTradeType(
        request.getTradeType(), UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.just(signpostingStepByTradeTypeForINAndAU));
      when(signpostingStepRepository.findByTradeTypeAndCommodityHierarchyCodes(
        request.getTradeType(),
        List.of(COMMODITY_CODE, HEADING_CODE),
        UserType.DECLARING_TRADER,
        Locale.EN))
        .thenReturn(Flux.just(signpostingStepByTradeTypeAndCommodityForINAndAU));
      when(signpostingStepRepository.findByTradeTypeAndSection(
        request.getTradeType(), SECTION_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.just(signpostingStepByTradeTypeAndSectionForINAndAU));
      when(signpostingStepRepository.findByTradeTypeAndChapter(
        request.getTradeType(), CHAPTER_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.just(signpostingStepByTradeTypeAndChapterForINAndAU));
      when(measureTypeService.getSignpostingMeasureTypeContents(
        measureList, request.getCommodityCode(), destinationCountryGB))
        .thenReturn(Flux.empty());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(
            List.of(
              CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
              CommodityChapter.builder()
                .id(String.valueOf(CHAPTER_ID))
                .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
                .build(),
              CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build()))
          .build();

      List<SignpostingContent> signpostingContents =
        signpostingContentService
          .getSignpostingContents(request, tradeTariffCommodityResponse, measureList)
          .block();

      assertThat(signpostingContents).isNotNull().hasSize(1);
      assertThat(signpostingContents.get(0).getHeaders()).hasSize(1);
      assertThat(signpostingContents.get(0).getHeaders().get(0).getSteps())
        .containsExactlyInAnyOrder(
          SignpostingStepResponse.builder()
            .id(1)
            .stepDescription("Content assigned to any commodity from IN and AU")
            .build(),
          SignpostingStepResponse.builder()
            .id(2)
            .stepDescription("Content assigned to commodity for IN and AU")
            .build(),
          SignpostingStepResponse.builder()
            .id(3)
            .stepDescription("Content assigned to section for IN and AU")
            .build(),
          SignpostingStepResponse.builder()
            .id(4)
            .stepDescription("Content assigned to chapter for IN and AU")
            .build());
    }

    private void setUpMockedResponses(
      UkCountry destinationCountryGB,
      CommodityMeasuresRequest request,
      List<Measure> measureList) {
      final SignpostingStepHeader signpostingStepHeader =
        SignpostingStepHeader.builder()
          .id(1)
          .relatedEntityType(null)
          .orderIndex(1)
          .description("Get your business ready")
          .build();

      SignpostingStep signpostingStepByTradeTypeForEU =
        SignpostingStep.builder()
          .id(1)
          .stepDescription("Content assigned to any commodity for IMPORT from EU")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("EU"))
          .build();

      SignpostingStep signpostingStepByTradeTypeForFR =
        SignpostingStep.builder()
          .id(2)
          .stepDescription("Content assigned to any commodity for IMPORT from FR")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("FR"))
          .build();

      SignpostingStep signpostingStepByTradeTypeForNonEUIndia =
        SignpostingStep.builder()
          .id(3)
          .stepDescription("Content assigned to any commodity for IMPORT from IN")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("IN"))
          .build();

      SignpostingStep signpostingStepByTradeTypeForGlobal =
        SignpostingStep.builder()
          .id(4)
          .stepDescription("Content assigned to any commodity for IMPORT from all countries")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("ALL"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndCommodityForEU =
        SignpostingStep.builder()
          .id(5)
          .stepDescription("Content assigned to commodity from EU")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("EU"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndCommodityForFR =
        SignpostingStep.builder()
          .id(6)
          .stepDescription("Content assigned to commodity from FR")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("FR"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndCommodityForNonEUIndia =
        SignpostingStep.builder()
          .id(7)
          .stepDescription("Content assigned to commodity from IN")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("IN"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndCommodityForGlobal =
        SignpostingStep.builder()
          .id(8)
          .stepDescription("Content assigned to commodity from all countries")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("ALL"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndSectionForEU =
        SignpostingStep.builder()
          .id(9)
          .stepDescription("Content assigned to section from EU")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("EU"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndSectionForFR =
        SignpostingStep.builder()
          .id(10)
          .stepDescription("Content assigned to section from FR")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("FR"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndSectionForNonEUIndia =
        SignpostingStep.builder()
          .id(11)
          .stepDescription("Content assigned to section from IN")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("IN"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndSectionForGlobal =
        SignpostingStep.builder()
          .id(12)
          .stepDescription("Content assigned to section from all countries")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("ALL"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndChapterForEU =
        SignpostingStep.builder()
          .id(13)
          .stepDescription("Content assigned to chapter from EU")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("EU"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndChapterForFR =
        SignpostingStep.builder()
          .id(14)
          .stepDescription("Content assigned to chapter from FR")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("FR"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndChapterForNonEUIndia =
        SignpostingStep.builder()
          .id(15)
          .stepDescription("Content assigned to chapter from IN")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("IN"))
          .build();

      SignpostingStep signpostingStepByTradeTypeAndChapterForGlobal =
        SignpostingStep.builder()
          .id(16)
          .stepDescription("Content assigned to chapter from all countries")
          .header(signpostingStepHeader)
          .originCountryRestrictions(Set.of("ALL"))
          .build();

      when(signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
        UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(
          Flux.just(
            SignpostingSuperHeader.builder()
              .id(1)
              .orderIndex(1)
              .header(signpostingStepHeader)
              .build()));
      when(signpostingStepRepository.findByTradeType(
        request.getTradeType(), UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(
          Flux.just(
            signpostingStepByTradeTypeForEU,
            signpostingStepByTradeTypeForFR,
            signpostingStepByTradeTypeForNonEUIndia,
            signpostingStepByTradeTypeForGlobal));
      when(signpostingStepRepository.findByTradeTypeAndCommodityHierarchyCodes(
        request.getTradeType(),
        List.of(COMMODITY_CODE, HEADING_CODE),
        UserType.DECLARING_TRADER,
        Locale.EN))
        .thenReturn(
          Flux.just(
            signpostingStepByTradeTypeAndCommodityForEU,
            signpostingStepByTradeTypeAndCommodityForFR,
            signpostingStepByTradeTypeAndCommodityForNonEUIndia,
            signpostingStepByTradeTypeAndCommodityForGlobal));
      when(signpostingStepRepository.findByTradeTypeAndSection(
        request.getTradeType(), SECTION_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(
          Flux.just(
            signpostingStepByTradeTypeAndSectionForEU,
            signpostingStepByTradeTypeAndSectionForFR,
            signpostingStepByTradeTypeAndSectionForNonEUIndia,
            signpostingStepByTradeTypeAndSectionForGlobal));
      when(signpostingStepRepository.findByTradeTypeAndChapter(
        request.getTradeType(), CHAPTER_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(
          Flux.just(
            signpostingStepByTradeTypeAndChapterForEU,
            signpostingStepByTradeTypeAndChapterForFR,
            signpostingStepByTradeTypeAndChapterForNonEUIndia,
            signpostingStepByTradeTypeAndChapterForGlobal));
      when(measureTypeService.getSignpostingMeasureTypeContents(
        measureList, request.getCommodityCode(), destinationCountryGB))
        .thenReturn(Flux.empty());
    }
  }

  @Nested
  class GetSignpostingContentsBasedOnHierarchyCodes {

    @Test
    @DisplayName(
      "should get steps for commodity hierarchy - heading, ancestors and linked commodities")
    void shouldGetStepsForCommodityHierarchy() {
      final String commodityCode = "2007109995";
      UkCountry destinationCountryGB = UkCountry.GB;

      CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode(commodityCode)
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .originCountry("IN")
          .destinationCountry(destinationCountryGB)
          .build();

      Measure measureWithMeasureConditions =
        Measure.builder()
          .measureType(
            MeasureType.builder()
              .id("750")
              .description("Import control of organic products")
              .build())
          .measureConditions(List.of(MeasureCondition.builder().documentCode("C644").build()))
          .build();
      List<Measure> measureList = List.of(measureWithMeasureConditions);

      final String ancestorCode1 = "2007100000";
      final String ancestorCode2 = "2007109100";
      final List<TradeTariffCommodityResponseIncludedEntity> included =
        List.of(
          CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
          CommodityChapter.builder()
            .id(String.valueOf(CHAPTER_ID))
            .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
            .build(),
          CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build(),
          CommodityImpl.builder().goodsNomenclatureItemId(ancestorCode1).build(),
          CommodityImpl.builder().goodsNomenclatureItemId(ancestorCode2).build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(commodityCode)
              .build())
          .included(included)
          .build();

      when(signpostingStepRepository.findByTradeType(
        any(TradeType.class), any(UserType.class), any(Locale.class)))
        .thenReturn(Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndSection(
        any(TradeType.class), any(Integer.class), any(UserType.class), any(Locale.class)))
        .thenReturn(Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndChapter(
        any(TradeType.class), any(Integer.class), any(UserType.class), any(Locale.class)))
        .thenReturn(Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndCommodityHierarchyCodes(
        any(TradeType.class), any(List.class), eq(UserType.DECLARING_TRADER), eq(Locale.EN)))
        .thenReturn(Flux.just(SignpostingStep.builder().build()));
      when(signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
        UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.empty());
      when(measureTypeService.getSignpostingMeasureTypeContents(
        measureList, request.getCommodityCode(), destinationCountryGB))
        .thenReturn(Flux.empty());

      signpostingContentService.getSignpostingContents(
        request, tradeTariffCommodityResponse, measureList);

      final ArgumentCaptor<List<String>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
      verify(signpostingStepRepository)
        .findByTradeTypeAndCommodityHierarchyCodes(
          eq(TradeType.IMPORT),
          listArgumentCaptor.capture(),
          eq(UserType.DECLARING_TRADER),
          eq(Locale.EN));

      assertThat(listArgumentCaptor.getValue())
        .containsExactlyInAnyOrder(commodityCode, HEADING_CODE, ancestorCode1, ancestorCode2);
    }

    @Test
    @DisplayName("should get steps for commodity which are also a heading")
    void shouldGetStepsForCommodityWhichAreAlsoHeading() {
      final String commodityCode = "2007109995";
      UkCountry destinationCountryGB = UkCountry.GB;

      CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode(commodityCode)
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .originCountry("IN")
          .destinationCountry(destinationCountryGB)
          .build();

      Measure measureWithMeasureConditions =
        Measure.builder()
          .measureType(
            MeasureType.builder()
              .id("750")
              .description("Import control of organic products")
              .build())
          .measureConditions(List.of(MeasureCondition.builder().documentCode("C644").build()))
          .build();
      List<Measure> measureList = List.of(measureWithMeasureConditions);

      final List<TradeTariffCommodityResponseIncludedEntity> included =
        List.of(
          CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
          CommodityChapter.builder()
            .id(String.valueOf(CHAPTER_ID))
            .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
            .build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(commodityCode)
              .build())
          .included(included)
          .build();

      when(signpostingStepRepository.findByTradeType(
        any(TradeType.class), any(UserType.class), any(Locale.class)))
        .thenReturn(Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndSection(
        any(TradeType.class), any(Integer.class), any(UserType.class), any(Locale.class)))
        .thenReturn(Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndChapter(
        any(TradeType.class), any(Integer.class), any(UserType.class), any(Locale.class)))
        .thenReturn(Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndCommodityHierarchyCodes(
        any(TradeType.class), any(List.class), eq(UserType.DECLARING_TRADER), eq(Locale.EN)))
        .thenReturn(Flux.just(SignpostingStep.builder().build()));
      when(signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
        UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.empty());
      when(measureTypeService.getSignpostingMeasureTypeContents(
        measureList, request.getCommodityCode(), destinationCountryGB))
        .thenReturn(Flux.empty());

      signpostingContentService.getSignpostingContents(
        request, tradeTariffCommodityResponse, measureList);

      final ArgumentCaptor<List<String>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
      verify(signpostingStepRepository)
        .findByTradeTypeAndCommodityHierarchyCodes(
          eq(TradeType.IMPORT),
          listArgumentCaptor.capture(),
          eq(UserType.DECLARING_TRADER),
          eq(Locale.EN));

      assertThat(listArgumentCaptor.getValue()).containsExactlyInAnyOrder(commodityCode);
    }
  }

  @Nested
  class SignpostingContentSuperHeaders {

    @Test
    @DisplayName("should get all super headers and assigned headers")
    void shouldGetAllSuperHeadersAndAssignedHeaders() {
      UkCountry destinationCountryGB = UkCountry.GB;

      CommodityMeasuresRequest request =
        CommodityMeasuresRequest.builder()
          .commodityCode("2804501000")
          .tradeType(TradeType.IMPORT)
          .userType(UserType.DECLARING_TRADER)
          .originCountry("FR")
          .destinationCountry(destinationCountryGB)
          .build();

      final SignpostingStepHeader header1UnderSuperHeader1 =
        SignpostingStepHeader.builder()
          .id(1)
          .relatedEntityType("IMPORT_RECORD_KEEPING")
          .orderIndex(1)
          .description("Record keeping header")
          .externalLink("external link")
          .build();

      final SignpostingStepHeader header2UnderSuperHeader2 =
        SignpostingStepHeader.builder()
          .id(2)
          .relatedEntityType("IMPORT_DECLARATION")
          .orderIndex(1)
          .description("Declaration header")
          .build();

      SignpostingSuperHeader superHeader1 =
        SignpostingSuperHeader.builder()
          .id(1)
          .orderIndex(1)
          .description("description 1")
          .explanatoryText("sub text 1")
          .header(header1UnderSuperHeader1)
          .build();
      SignpostingSuperHeader superHeader2 =
        SignpostingSuperHeader.builder()
          .id(2)
          .orderIndex(2)
          .description("description 2")
          .explanatoryText("sub text 2")
          .header(header2UnderSuperHeader2)
          .build();

      List<SignpostingStep> signpostingStepsByTradeType =
        List.of(
          SignpostingStep.builder()
            .id(1)
            .stepDescription("Content assigned to any commodity for IMPORT")
            .header(header1UnderSuperHeader1)
            .build(),
          SignpostingStep.builder()
            .id(2)
            .stepDescription("Content 2 assigned to any commodity for IMPORT")
            .header(header2UnderSuperHeader2)
            .build());

      when(signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
        UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.just(superHeader1, superHeader2));

      when(signpostingStepRepository.findByTradeType(
        request.getTradeType(), UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.fromIterable(signpostingStepsByTradeType));

      when(signpostingStepRepository.findByTradeTypeAndCommodityHierarchyCodes(
        request.getTradeType(),
        List.of(COMMODITY_CODE, HEADING_CODE),
        UserType.DECLARING_TRADER,
        Locale.EN))
        .thenReturn(Flux.empty());

      when(signpostingStepRepository.findByTradeTypeAndSection(
        request.getTradeType(), SECTION_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.empty());

      when(signpostingStepRepository.findByTradeTypeAndChapter(
        request.getTradeType(), CHAPTER_ID, UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.empty());

      when(measureTypeService.getSignpostingMeasureTypeContents(
        List.of(), request.getCommodityCode(), destinationCountryGB))
        .thenReturn(Flux.empty());

      final List<TradeTariffCommodityResponseIncludedEntity> included =
        List.of(
          CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
          CommodityChapter.builder()
            .id(String.valueOf(CHAPTER_ID))
            .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
            .build(),
          CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
        TradeTariffCommodityResponse.builder()
          .data(
            TradeTariffCommodityResponseData.builder()
              .id("1234")
              .type("commodity")
              .formattedDescription("description")
              .goodsNomenclatureItemId(COMMODITY_CODE)
              .build())
          .included(included)
          .build();

      List<SignpostingContent> signpostingContents =
        signpostingContentService
          .getSignpostingContents(request, tradeTariffCommodityResponse, List.of())
          .block();

      assertThat(signpostingContents).isNotNull().hasSize(2);
      SignpostingContent superHeaderContent1 = signpostingContents.get(0);
      assertThat(superHeaderContent1.getSuperHeader()).isNotNull();
      assertThat(superHeaderContent1.getSuperHeader())
        .isEqualTo(
          SuperHeader.builder()
            .description(superHeader1.getDescription())
            .explanatoryText(superHeader1.getExplanatoryText())
            .orderIndex(superHeader1.getOrderIndex())
            .build());
      assertThat(superHeaderContent1.getHeaders()).hasSize(1);
      assertThat(superHeaderContent1.getHeaders().get(0).getHeader())
        .isEqualTo(
          Header.builder()
            .id(header1UnderSuperHeader1.getId())
            .orderIndex(header1UnderSuperHeader1.getOrderIndex())
            .description(header1UnderSuperHeader1.getDescription())
            .explanatoryText(header1UnderSuperHeader1.getExplanatoryText())
            .linkText(header1UnderSuperHeader1.getLinkText())
            .externalLink(header1UnderSuperHeader1.getExternalLink())
            .relatedTo(header1UnderSuperHeader1.getRelatedEntityType())
            .build());
      SignpostingContent superHeaderContent2 = signpostingContents.get(1);
      assertThat(superHeaderContent2.getSuperHeader()).isNotNull();
      assertThat(superHeaderContent2.getSuperHeader())
        .isEqualTo(
          SuperHeader.builder()
            .description(superHeader2.getDescription())
            .explanatoryText(superHeader2.getExplanatoryText())
            .orderIndex(superHeader2.getOrderIndex())
            .build());
      assertThat(superHeaderContent2.getHeaders()).hasSize(1);
      assertThat(superHeaderContent2.getHeaders().get(0).getHeader())
        .isEqualTo(
          Header.builder()
            .id(header2UnderSuperHeader2.getId())
            .orderIndex(header2UnderSuperHeader2.getOrderIndex())
            .description(header2UnderSuperHeader2.getDescription())
            .explanatoryText(header2UnderSuperHeader2.getExplanatoryText())
            .linkText(header2UnderSuperHeader2.getLinkText())
            .externalLink(header2UnderSuperHeader2.getExternalLink())
            .relatedTo(header2UnderSuperHeader2.getRelatedEntityType())
            .build());
    }
  }

  @Nested
  class Measures {
    @Test
    void shouldSetMeasuresOnHeaderAssociatedWithCertificates(){
      final SignpostingStepHeader signpostingStepHeader =
          SignpostingStepHeader.builder()
              .id(1)
              .relatedEntityType(null)
              .orderIndex(1)
              .description("Get your business ready")
              .linkText("Register to bring goods across border")
              .build();
      final SignpostingStepHeader signpostingStepHeaderWithComplexMeasures =
          SignpostingStepHeader.builder()
              .id(4)
              .relatedEntityType("IMPORT_CONTROLS")
              .orderIndex(2)
              .description("Check if you need an import license or certificate")
              .linkText("Licences certificates restrictions")
              .build();
      final Header expectedHeaderWithStepsAndWithoutComplexMeasures =
          Header.builder()
              .id(signpostingStepHeader.getId())
              .orderIndex(signpostingStepHeader.getOrderIndex())
              .description(signpostingStepHeader.getDescription())
              .linkText(signpostingStepHeader.getLinkText())
              .build();

      final Header expectedHeaderWithoutStepAndWithComplexMeasures =
          Header.builder()
              .id(signpostingStepHeaderWithComplexMeasures.getId())
              .orderIndex(signpostingStepHeaderWithComplexMeasures.getOrderIndex())
              .description(signpostingStepHeaderWithComplexMeasures.getDescription())
              .linkText(signpostingStepHeaderWithComplexMeasures.getLinkText())
              .relatedTo("IMPORT_CONTROLS")
              .build();

      UkCountry destinationCountryGB = UkCountry.GB;

      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("2804501000")
              .tradeType(TradeType.IMPORT)
              .userType(UserType.DECLARING_TRADER)
              .destinationCountry(destinationCountryGB)
              .originCountry("FR")
              .build();

      Measure measureWithoutDocumentCode =
          Measure.builder()
              .measureType(MeasureType.builder().id("VTZ").description("VAT zero rate").build())
              .measureConditions(List.of(MeasureCondition.builder().build()))
              .build();

      Measure measureWithMeasureConditions =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id("750")
                      .description("Import control of organic products")
                      .build())
              .measureConditions(List.of(MeasureCondition.builder().documentCode("C644").build()))
              .build();
      List<Measure> measureList = List.of(measureWithoutDocumentCode, measureWithMeasureConditions);

      String superHeaderDescription = "super header 1";
      int superHeaderId = 1;
      int superHeaderOrderIndex = 1;
      String superHeaderExplanatoryText = "super header explanatory text";
      SignpostingSuperHeaderBuilder superHeaderBuilder =
          SignpostingSuperHeader.builder()
              .id(superHeaderId)
              .orderIndex(superHeaderOrderIndex)
              .description(superHeaderDescription)
              .explanatoryText(superHeaderExplanatoryText);
      SignpostingSuperHeader superHeader = superHeaderBuilder.header(signpostingStepHeader).build();
      SignpostingSuperHeader superHeaderLinkedToComplexMeasures =
          superHeaderBuilder.header(signpostingStepHeaderWithComplexMeasures).build();

      when(signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
          UserType.DECLARING_TRADER, Locale.EN))
          .thenReturn(Flux.just(superHeader, superHeaderLinkedToComplexMeasures));

      when(signpostingStepRepository.findByTradeType(
          request.getTradeType(), UserType.DECLARING_TRADER, Locale.EN))
          .thenReturn(Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndCommodityHierarchyCodes(
          request.getTradeType(),
          List.of(COMMODITY_CODE, HEADING_CODE),
          UserType.DECLARING_TRADER,
          Locale.EN))
          .thenReturn(
              Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndSection(
          request.getTradeType(), SECTION_ID, UserType.DECLARING_TRADER, Locale.EN))
          .thenReturn(Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndChapter(
          request.getTradeType(), CHAPTER_ID, UserType.DECLARING_TRADER, Locale.EN))
          .thenReturn(Flux.empty());
      ConditionBasedRestrictiveMeasure measureTypeContent = ConditionBasedRestrictiveMeasure.builder().build();
      when(measureTypeService.getSignpostingMeasureTypeContents(
          measureList, request.getCommodityCode(), destinationCountryGB))
          .thenReturn(Flux.just(measureTypeContent));

      final List<TradeTariffCommodityResponseIncludedEntity> included =
          List.of(
              CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
              CommodityChapter.builder()
                  .id(String.valueOf(CHAPTER_ID))
                  .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
                  .build(),
              CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
          TradeTariffCommodityResponse.builder()
              .data(
                  TradeTariffCommodityResponseData.builder()
                      .id("1234")
                      .type("commodity")
                      .formattedDescription("description")
                      .goodsNomenclatureItemId(COMMODITY_CODE)
                      .build())
              .included(included)
              .build();

      List<SignpostingContent> signpostingContents =
          signpostingContentService
              .getSignpostingContents(request, tradeTariffCommodityResponse, measureList)
              .block();

      assertThat(signpostingContents).isNotNull().hasSize(1);

      SignpostingContent superHeaderContent = signpostingContents.get(0);
      assertThat(superHeaderContent.getSuperHeader().getDescription())
          .isEqualTo(superHeaderDescription);
      assertThat(superHeaderContent.getSuperHeader().getExplanatoryText())
          .isEqualTo(superHeaderExplanatoryText);
      assertThat(superHeaderContent.getSuperHeader().getOrderIndex())
          .isEqualTo(superHeaderOrderIndex);

      // check first header with steps
      assertThat(superHeaderContent.getHeaders().get(0).getHeader())
          .isEqualTo(expectedHeaderWithStepsAndWithoutComplexMeasures);
      // check first header with complex measures
      assertThat(superHeaderContent.getHeaders().get(1).getHeader())
          .isEqualTo(expectedHeaderWithoutStepAndWithComplexMeasures);
      assertThat(superHeaderContent.getHeaders().get(1).getSteps()).isEmpty();
      assertThat(superHeaderContent.getHeaders().get(1).getMeasures())
          .containsExactly((measureTypeContent));
    }

    @Test
    void shouldNotReturnProhibitedMeasuresIfAny(){
      final SignpostingStepHeader signpostingStepHeader =
          SignpostingStepHeader.builder()
              .id(1)
              .relatedEntityType(null)
              .orderIndex(1)
              .description("Get your business ready")
              .linkText("Register to bring goods across border")
              .build();
      final SignpostingStepHeader signpostingStepHeaderWithComplexMeasures =
          SignpostingStepHeader.builder()
              .id(4)
              .relatedEntityType("IMPORT_CONTROLS")
              .orderIndex(2)
              .description("Check if you need an import license or certificate")
              .linkText("Licences certificates restrictions")
              .build();
      final Header expectedHeaderWithStepsAndWithoutComplexMeasures =
          Header.builder()
              .id(signpostingStepHeader.getId())
              .orderIndex(signpostingStepHeader.getOrderIndex())
              .description(signpostingStepHeader.getDescription())
              .linkText(signpostingStepHeader.getLinkText())
              .build();

      final Header expectedHeaderWithoutStepAndWithComplexMeasures =
          Header.builder()
              .id(signpostingStepHeaderWithComplexMeasures.getId())
              .orderIndex(signpostingStepHeaderWithComplexMeasures.getOrderIndex())
              .description(signpostingStepHeaderWithComplexMeasures.getDescription())
              .linkText(signpostingStepHeaderWithComplexMeasures.getLinkText())
              .relatedTo("IMPORT_CONTROLS")
              .build();

      UkCountry destinationCountryGB = UkCountry.GB;

      CommodityMeasuresRequest request =
          CommodityMeasuresRequest.builder()
              .commodityCode("2804501000")
              .tradeType(TradeType.IMPORT)
              .userType(UserType.DECLARING_TRADER)
              .destinationCountry(destinationCountryGB)
              .originCountry("FR")
              .build();

      Measure measureWithoutDocumentCode =
          Measure.builder()
              .measureType(MeasureType.builder().id("VTZ").description("VAT zero rate").build())
              .measureConditions(List.of(MeasureCondition.builder().build()))
              .build();

      Measure measureWithMeasureConditions =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id("750")
                      .description("Import control of organic products")
                      .build())
              .measureConditions(List.of(MeasureCondition.builder().documentCode("C644").build()))
              .build();
      List<Measure> measureList = List.of(measureWithoutDocumentCode, measureWithMeasureConditions);

      String superHeaderDescription = "super header 1";
      int superHeaderId = 1;
      int superHeaderOrderIndex = 1;
      String superHeaderExplanatoryText = "super header explanatory text";
      SignpostingSuperHeaderBuilder superHeaderBuilder =
          SignpostingSuperHeader.builder()
              .id(superHeaderId)
              .orderIndex(superHeaderOrderIndex)
              .description(superHeaderDescription)
              .explanatoryText(superHeaderExplanatoryText);
      SignpostingSuperHeader superHeader = superHeaderBuilder.header(signpostingStepHeader).build();
      SignpostingSuperHeader superHeaderLinkedToComplexMeasures =
          superHeaderBuilder.header(signpostingStepHeaderWithComplexMeasures).build();

      when(signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
          UserType.DECLARING_TRADER, Locale.EN))
          .thenReturn(Flux.just(superHeader, superHeaderLinkedToComplexMeasures));

      when(signpostingStepRepository.findByTradeType(
          request.getTradeType(), UserType.DECLARING_TRADER, Locale.EN))
          .thenReturn(Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndCommodityHierarchyCodes(
          request.getTradeType(),
          List.of(COMMODITY_CODE, HEADING_CODE),
          UserType.DECLARING_TRADER,
          Locale.EN))
          .thenReturn(
              Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndSection(
          request.getTradeType(), SECTION_ID, UserType.DECLARING_TRADER, Locale.EN))
          .thenReturn(Flux.empty());
      when(signpostingStepRepository.findByTradeTypeAndChapter(
          request.getTradeType(), CHAPTER_ID, UserType.DECLARING_TRADER, Locale.EN))
          .thenReturn(Flux.empty());
      ConditionBasedRestrictiveMeasure conditionBasedRestrictiveMeasure = ConditionBasedRestrictiveMeasure.builder().build();
      when(measureTypeService.getSignpostingMeasureTypeContents(
          measureList, request.getCommodityCode(), destinationCountryGB))
          .thenReturn(Flux.just(conditionBasedRestrictiveMeasure, Prohibition.builder().build()));

      final List<TradeTariffCommodityResponseIncludedEntity> included =
          List.of(
              CommoditySection.builder().id(String.valueOf(SECTION_ID)).build(),
              CommodityChapter.builder()
                  .id(String.valueOf(CHAPTER_ID))
                  .goodsNomenclatureItemId(CHAPTER_GOODS_NOMENCLATURE_ITEM_ID)
                  .build(),
              CommodityHeading.builder().goodsNomenclatureItemId(HEADING_CODE).build());

      TradeTariffCommodityResponse tradeTariffCommodityResponse =
          TradeTariffCommodityResponse.builder()
              .data(
                  TradeTariffCommodityResponseData.builder()
                      .id("1234")
                      .type("commodity")
                      .formattedDescription("description")
                      .goodsNomenclatureItemId(COMMODITY_CODE)
                      .build())
              .included(included)
              .build();

      List<SignpostingContent> signpostingContents =
          signpostingContentService
              .getSignpostingContents(request, tradeTariffCommodityResponse, measureList)
              .block();

      assertThat(signpostingContents).isNotNull().hasSize(1);

      SignpostingContent superHeaderContent = signpostingContents.get(0);
      assertThat(superHeaderContent.getSuperHeader().getDescription())
          .isEqualTo(superHeaderDescription);
      assertThat(superHeaderContent.getSuperHeader().getExplanatoryText())
          .isEqualTo(superHeaderExplanatoryText);
      assertThat(superHeaderContent.getSuperHeader().getOrderIndex())
          .isEqualTo(superHeaderOrderIndex);

      // check first header with steps
      assertThat(superHeaderContent.getHeaders().get(0).getHeader())
          .isEqualTo(expectedHeaderWithStepsAndWithoutComplexMeasures);
      // check first header with complex measures
      assertThat(superHeaderContent.getHeaders().get(1).getHeader())
          .isEqualTo(expectedHeaderWithoutStepAndWithComplexMeasures);
      assertThat(superHeaderContent.getHeaders().get(1).getSteps()).isEmpty();
      assertThat(superHeaderContent.getHeaders().get(1).getMeasures())
          .containsExactly(conditionBasedRestrictiveMeasure);
    }
  }
}
