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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType.IMPORT;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.MeasureTypeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.MeasureTypeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.ConditionBasedRestrictiveMeasure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentCodeMeasureOption;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentaryMeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptions;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.RestrictiveMeasure;

@ExtendWith(MockitoExtension.class)
public class MeasureTypeServiceTest {

  private static final String MEASURE_TYPE_ID = "1";
  private static final String DEFAULT_DESCRIPTION = "default description";
  private static final String DESCRIPTION_FROM_CONTENT_API = "description from Content API";

  private final String commodityCode = "123";

  @Mock private MeasureTypeDescriptionRepository measureTypeDescriptionRepository;
  @Mock private MeasureOptionService measureOptionService;

  @InjectMocks private MeasureTypeService measureTypeService;

  @Nested
  class GetRestrictiveMeasures {

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("should discard measure options which are empty")
    void shouldDiscardEmptyMeasureOptions(Locale locale) {
      List<MeasureCondition> measureConditions1 =
          List.of(
              DocumentaryMeasureCondition.builder().conditionCode(MeasureConditionCode.M).build());
      List<MeasureCondition> measureConditions2 =
          List.of(
              DocumentaryMeasureCondition.builder().conditionCode(MeasureConditionCode.N).build());
      Measure measure1 =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id(MEASURE_TYPE_ID)
                      .description(DEFAULT_DESCRIPTION)
                      .build())
              .measureConditions(measureConditions1)
              .build();
      Measure measure2 =
          Measure.builder()
              .measureType(MeasureType.builder().id("2").description("desc").build())
              .measureConditions(measureConditions2)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure1.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId(MEASURE_TYPE_ID)
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build()));

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure2.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId("2")
                      .descriptionOverlay("desc DB")
                      .build()));

      MeasureOptions measureOptions1 = MeasureOptions.builder().build();
      when(measureOptionService.getMeasureOptions(measureConditions1, IMPORT, locale))
          .thenReturn(Flux.just(measureOptions1));
      when(measureOptionService.getMeasureOptions(measureConditions2, IMPORT, locale))
          .thenReturn(Flux.empty());

      StepVerifier.create(
              measureTypeService.getRestrictiveMeasures(
                  List.of(measure1, measure2), commodityCode, IMPORT, locale))
          .expectNext(
              ConditionBasedRestrictiveMeasure.builder()
                  .id(MEASURE_TYPE_ID)
                  .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                  .description(DESCRIPTION_FROM_CONTENT_API)
                  .measureOptions(List.of(measureOptions1))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should return content when description has been setup for requested destination country with measure option")
    void shouldReturnContentWhenDescriptionHasBeenSetupForDestinationCountryWithMeasureOption(
        Locale locale) {
      List<MeasureCondition> measureConditions =
          List.of(DocumentaryMeasureCondition.builder().build());
      String seriesId = "A";
      Measure measure =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id(MEASURE_TYPE_ID)
                      .seriesId(seriesId)
                      .description(DEFAULT_DESCRIPTION)
                      .build())
              .measureConditions(measureConditions)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId(MEASURE_TYPE_ID)
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build()));

      MeasureOptions measureOptions = MeasureOptions.builder().build();
      when(measureOptionService.getMeasureOptions(measureConditions, IMPORT, locale))
          .thenReturn(Flux.just(measureOptions));

      StepVerifier.create(
              measureTypeService.getRestrictiveMeasures(
                  List.of(measure), commodityCode, IMPORT, locale))
          .expectNext(
              ConditionBasedRestrictiveMeasure.builder()
                  .id(MEASURE_TYPE_ID)
                  .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                  .description(DESCRIPTION_FROM_CONTENT_API)
                  .measureTypeSeries(seriesId)
                  .measureOptions(List.of(measureOptions))
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should return content when description has been setup for more than one destination country with measure option")
    void
        shouldReturnContentWhenDescriptionHasBeenSetupForMoreThanOneDestinationCountryWithMeasureOption(
            Locale locale) {
      List<MeasureCondition> measureConditions =
          List.of(DocumentaryMeasureCondition.builder().build());
      String seriesId = "A";
      Measure measure =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id(MEASURE_TYPE_ID)
                      .description(DEFAULT_DESCRIPTION)
                      .seriesId(seriesId)
                      .build())
              .measureConditions(measureConditions)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId(MEASURE_TYPE_ID)
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build()));

      DocumentCodeMeasureOption expectedMeasureOption =
          DocumentCodeMeasureOption.builder()
              .documentCodeDescription(DocumentCodeDescription.builder().build())
              .build();
      when(measureOptionService.getMeasureOptions(measureConditions, IMPORT, locale))
          .thenReturn(
              Flux.just(MeasureOptions.builder().options(List.of(expectedMeasureOption)).build()));

      StepVerifier.create(
              measureTypeService.getRestrictiveMeasures(
                  List.of(measure), commodityCode, IMPORT, locale))
          .expectNext(
              ConditionBasedRestrictiveMeasure.builder()
                  .id(MEASURE_TYPE_ID)
                  .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                  .description(DESCRIPTION_FROM_CONTENT_API)
                  .measureOptions(
                      List.of(
                          MeasureOptions.builder().options(List.of(expectedMeasureOption)).build()))
                  .measureTypeSeries(seriesId)
                  .build())
          .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should return content when no description has been setup for requested destination country")
    void shouldReturnContentWhenNoDescriptionHasBeenSetupForDestinationCountry(Locale locale) {
      List<MeasureCondition> measureConditions =
          List.of(DocumentaryMeasureCondition.builder().build());
      String seriesId = "A";
      Measure measure =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id(MEASURE_TYPE_ID)
                      .description(DEFAULT_DESCRIPTION)
                      .seriesId(seriesId)
                      .build())
              .measureConditions(measureConditions)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId(MEASURE_TYPE_ID)
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build()));

      DocumentCodeMeasureOption expectedMeasureOption =
          DocumentCodeMeasureOption.builder()
              .documentCodeDescription(DocumentCodeDescription.builder().build())
              .build();
      when(measureOptionService.getMeasureOptions(measureConditions, IMPORT, locale))
          .thenReturn(
              Flux.just(MeasureOptions.builder().options(List.of(expectedMeasureOption)).build()));

      List<RestrictiveMeasure> measureTypeContent =
          measureTypeService
              .getRestrictiveMeasures(List.of(measure), commodityCode, IMPORT, locale)
              .collectList()
              .block();

      assertThat(measureTypeContent).isNotNull().hasSize(1);
      assertThat(measureTypeContent.get(0))
          .isEqualTo(
              ConditionBasedRestrictiveMeasure.builder()
                  .id(MEASURE_TYPE_ID)
                  .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                  .description(DESCRIPTION_FROM_CONTENT_API)
                  .measureOptions(
                      List.of(
                          MeasureOptions.builder().options(List.of(expectedMeasureOption)).build()))
                  .measureTypeSeries(seriesId)
                  .build());
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("should return content when no description has been setup for measure type")
    void shouldReturnContentWhenNoDescriptionHasBeenSetupForMeasureType(Locale locale) {
      List<MeasureCondition> measureConditions =
          List.of(DocumentaryMeasureCondition.builder().build());
      String seriesId = "A";
      Measure measure =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id(MEASURE_TYPE_ID)
                      .description(DEFAULT_DESCRIPTION)
                      .seriesId(seriesId)
                      .build())
              .measureConditions(measureConditions)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(Flux.empty());

      DocumentCodeMeasureOption expectedMeasureOption =
          DocumentCodeMeasureOption.builder()
              .documentCodeDescription(DocumentCodeDescription.builder().build())
              .build();
      when(measureOptionService.getMeasureOptions(measureConditions, IMPORT, locale))
          .thenReturn(
              Flux.just(MeasureOptions.builder().options(List.of(expectedMeasureOption)).build()));

      List<RestrictiveMeasure> measureTypeContent =
          measureTypeService
              .getRestrictiveMeasures(List.of(measure), commodityCode, IMPORT, locale)
              .collectList()
              .block();

      assertThat(measureTypeContent).isNotNull().hasSize(1);
      assertThat(measureTypeContent.get(0))
          .isEqualTo(
              ConditionBasedRestrictiveMeasure.builder()
                  .id(MEASURE_TYPE_ID)
                  .descriptionOverlay(DEFAULT_DESCRIPTION)
                  .description(DEFAULT_DESCRIPTION)
                  .measureOptions(
                      List.of(
                          MeasureOptions.builder().options(List.of(expectedMeasureOption)).build()))
                  .measureTypeSeries(seriesId)
                  .build());
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName("should raise error when more than one description has been setup")
    void shouldRaiseErrorWhenMoreThanOneDescriptionHasBeenSetupForTheRequestedDestinationCountry(
        Locale locale) {
      List<MeasureCondition> measureConditions =
          List.of(DocumentaryMeasureCondition.builder().build());
      Measure measure =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id(MEASURE_TYPE_ID)
                      .description(DEFAULT_DESCRIPTION)
                      .build())
              .measureConditions(measureConditions)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId(MEASURE_TYPE_ID)
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build(),
                  MeasureTypeDescription.builder()
                      .measureTypeId(MEASURE_TYPE_ID)
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build()));

      when(measureOptionService.getMeasureOptions(measureConditions, IMPORT, locale))
          .thenReturn(Flux.just(MeasureOptions.builder().build()));

      Mono<List<RestrictiveMeasure>> restrictiveMeasures =
          measureTypeService
              .getRestrictiveMeasures(List.of(measure), commodityCode, IMPORT, locale)
              .collectList();

      StepVerifier.create(restrictiveMeasures)
          .expectSubscription()
          .expectErrorMatches(
              error ->
                  error instanceof RuntimeException
                      && error
                          .getMessage()
                          .equals(
                              format(
                                  "More than one measure type descriptions configured for measure type %s, locale %s",
                                  measure.getMeasureType().getId(), locale)))
          .verify();
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should return hard coded content for 2309902000 commodity and measure type 465 when description has been setup")
    void
        shouldReturnHardCodedContentFor2309902000CommodityAndMeasureType465WhenDescriptionHasBeenSetupForDestinationCountryWithMeasureOption(
            Locale locale) {
      List<MeasureCondition> measureConditions =
          List.of(DocumentaryMeasureCondition.builder().build());
      String seriesId = "B";
      Measure measure =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id("465")
                      .description(DEFAULT_DESCRIPTION)
                      .seriesId(seriesId)
                      .build())
              .measureConditions(measureConditions)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId("465")
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build()));

      when(measureOptionService.getMeasureOptions(measureConditions, IMPORT, locale))
          .thenReturn(Flux.just(MeasureOptions.builder().build()));

      List<RestrictiveMeasure> measureTypeContent =
          measureTypeService
              .getRestrictiveMeasures(List.of(measure), "2309902000", IMPORT, locale)
              .collectList()
              .block();

      assertThat(measureTypeContent).isNotNull().hasSize(1);
      String expectedDescription =
          DESCRIPTION_FROM_CONTENT_API.concat(
              "%0A%0AIf you do not have a [certificate issued by the Federal Grain Inspection Service](https://www.legislation.gov.uk/eur/2007/1375/annex/I) and a [certificate issued by the USA wet milling industry](https://www.legislation.gov.uk/eur/2017/337/images/eur_20170337_2017-02-27_en_001?view=extent), your goods must go for laboratory analysis in the UK.%0A%0AIf you import residues from the manufacture of starch from maize from the USA, your goods will be subject to random sampling at the UK border if they’re accompanied by a [certificate issued by the Federal Grain Inspection Service](https://www.legislation.gov.uk/eur/2007/1375/annex/I)%0A%0AIn this case, you will need a [certificate issued by the USA wet milling industry](https://www.legislation.gov.uk/eur/2017/337/images/eur_20170337_2017-02-27_en_001?view=extent) when you import these goods.");
      assertThat(measureTypeContent.get(0))
          .isEqualTo(
              ConditionBasedRestrictiveMeasure.builder()
                  .id("465")
                  .descriptionOverlay(expectedDescription)
                  .description(expectedDescription)
                  .measureTypeSeries(seriesId)
                  .build());
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should return hard coded content for 2309902000 commodity and measure type 465 when no description has been setup for requested destination country")
    void
        shouldReturnHardCodedContentFor2309902000CommodityAndMeasureType465WhenNoDescriptionHasBeenSetupForDestinationCountry(
            Locale locale) {
      List<MeasureCondition> measureConditions =
          List.of(DocumentaryMeasureCondition.builder().build());
      String seriesId = "B";
      Measure measure =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id("465")
                      .description(DEFAULT_DESCRIPTION)
                      .seriesId(seriesId)
                      .build())
              .measureConditions(measureConditions)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId("465")
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build()));

      when(measureOptionService.getMeasureOptions(measureConditions, IMPORT, locale))
          .thenReturn(Flux.just(MeasureOptions.builder().build()));

      List<RestrictiveMeasure> measureTypeContent =
          measureTypeService
              .getRestrictiveMeasures(List.of(measure), "2309902000", IMPORT, locale)
              .collectList()
              .block();

      assertThat(measureTypeContent).isNotNull().hasSize(1);
      String expectedDescription =
          DESCRIPTION_FROM_CONTENT_API.concat(
              "%0A%0AIf you do not have a [certificate issued by the Federal Grain Inspection Service](https://www.legislation.gov.uk/eur/2007/1375/annex/I) and a [certificate issued by the USA wet milling industry](https://www.legislation.gov.uk/eur/2017/337/images/eur_20170337_2017-02-27_en_001?view=extent), your goods must go for laboratory analysis in the UK.%0A%0AIf you import residues from the manufacture of starch from maize from the USA, your goods will be subject to random sampling at the UK border if they’re accompanied by a [certificate issued by the Federal Grain Inspection Service](https://www.legislation.gov.uk/eur/2007/1375/annex/I)%0A%0AIn this case, you will need a [certificate issued by the USA wet milling industry](https://www.legislation.gov.uk/eur/2017/337/images/eur_20170337_2017-02-27_en_001?view=extent) when you import these goods.");
      assertThat(measureTypeContent.get(0))
          .isEqualTo(
              ConditionBasedRestrictiveMeasure.builder()
                  .id("465")
                  .descriptionOverlay(expectedDescription)
                  .description(expectedDescription)
                  .measureTypeSeries(seriesId)
                  .build());
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should return without hard coded content for any commodity with measure type 465 when description has been setup")
    void
        shouldReturnWithoutHardCodedContentForAnyCommodityWithMeasureType465WhenDescriptionHasBeenSetupForDestinationCountryWithMeasureOption(
            Locale locale) {
      List<MeasureCondition> measureConditions =
          List.of(DocumentaryMeasureCondition.builder().build());
      String seriesId = "B";
      Measure measure =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id("465")
                      .description(DEFAULT_DESCRIPTION)
                      .seriesId(seriesId)
                      .build())
              .measureConditions(measureConditions)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId("465")
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build()));

      MeasureOptions measureOptions = MeasureOptions.builder().build();
      when(measureOptionService.getMeasureOptions(measureConditions, IMPORT, locale))
          .thenReturn(Flux.just(measureOptions));

      List<RestrictiveMeasure> measureTypeContent =
          measureTypeService
              .getRestrictiveMeasures(List.of(measure), "2309902001", IMPORT, locale)
              .collectList()
              .block();

      assertThat(measureTypeContent).isNotNull().hasSize(1);
      assertThat(measureTypeContent.get(0))
          .isEqualTo(
              ConditionBasedRestrictiveMeasure.builder()
                  .id("465")
                  .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                  .description(DESCRIPTION_FROM_CONTENT_API)
                  .measureOptions(List.of(measureOptions))
                  .measureTypeSeries(seriesId)
                  .build());
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should return hard coded content for any commodity with measure type 362 when description has been setup")
    void
        shouldReturnHardCodedContentForAnyCommodityWithMeasureType362WhenDescriptionHasBeenSetupForDestinationCountryWithMeasureOption(
            Locale locale) {
      List<MeasureCondition> measureConditions =
          List.of(DocumentaryMeasureCondition.builder().build());
      String seriesId = "B";
      Measure measure =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id("362")
                      .description(DEFAULT_DESCRIPTION)
                      .seriesId(seriesId)
                      .build())
              .measureConditions(measureConditions)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId("362")
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build()));

      MeasureOptions measureOptions = MeasureOptions.builder().build();
      when(measureOptionService.getMeasureOptions(measureConditions, IMPORT, locale))
          .thenReturn(Flux.just(measureOptions));

      List<RestrictiveMeasure> measureTypeContent =
          measureTypeService
              .getRestrictiveMeasures(List.of(measure), commodityCode, IMPORT, locale)
              .collectList()
              .block();

      assertThat(measureTypeContent).isNotNull().hasSize(1);
      String expectedDescription =
          DESCRIPTION_FROM_CONTENT_API.concat(
              "%0A%0AIf you import precursor chemicals, you need authorisation from the Home Office. [Read about precursor chemical licensing](https://www.gov.uk/guidance/precursor-chemical-licensing)");
      assertThat(measureTypeContent.get(0))
          .isEqualTo(
              ConditionBasedRestrictiveMeasure.builder()
                  .id("362")
                  .descriptionOverlay(expectedDescription)
                  .description(expectedDescription)
                  .measureTypeSeries(seriesId)
                  .build());
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    @DisplayName(
        "should return hard coded content for any commodity with measure type 362 when no description has been setup for requested destination country")
    void
        shouldReturnHardCodedContentForAnyCommodityWithMeasureType362WhenNoDescriptionHasBeenSetupForDestinationCountry(
            Locale locale) {
      List<MeasureCondition> measureConditions =
          List.of(DocumentaryMeasureCondition.builder().build());
      String seriesId = "B";
      Measure measure =
          Measure.builder()
              .measureType(
                  MeasureType.builder()
                      .id("362")
                      .description(DEFAULT_DESCRIPTION)
                      .seriesId(seriesId)
                      .build())
              .measureConditions(measureConditions)
              .build();

      when(measureTypeDescriptionRepository
              .findMeasureTypeDescriptionsByMeasureTypeIdsAndTradeTypeAndLocale(
                  List.of(measure.getMeasureType().getId()), IMPORT, locale))
          .thenReturn(
              Flux.just(
                  MeasureTypeDescription.builder()
                      .measureTypeId("362")
                      .descriptionOverlay(DESCRIPTION_FROM_CONTENT_API)
                      .build()));

      MeasureOptions measureOptions = MeasureOptions.builder().build();
      when(measureOptionService.getMeasureOptions(measureConditions, IMPORT, locale))
          .thenReturn(Flux.just(measureOptions));

      List<RestrictiveMeasure> measureTypeContent =
          measureTypeService
              .getRestrictiveMeasures(List.of(measure), commodityCode, IMPORT, locale)
              .collectList()
              .block();

      assertThat(measureTypeContent).isNotNull().hasSize(1);
      String expectedDescription =
          DESCRIPTION_FROM_CONTENT_API.concat(
              "%0A%0AIf you import precursor chemicals, you need authorisation from the Home Office. [Read about precursor chemical licensing](https://www.gov.uk/guidance/precursor-chemical-licensing)");
      assertThat(measureTypeContent.get(0))
          .isEqualTo(
              ConditionBasedRestrictiveMeasure.builder()
                  .id("362")
                  .descriptionOverlay(expectedDescription)
                  .description(expectedDescription)
                  .measureTypeSeries(seriesId)
                  .build());
    }
  }
}
