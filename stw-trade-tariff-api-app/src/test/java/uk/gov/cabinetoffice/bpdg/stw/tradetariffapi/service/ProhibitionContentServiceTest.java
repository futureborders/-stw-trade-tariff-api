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
import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.RestrictiveMeasureType.PROHIBITIVE;

import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.ProhibitionDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.ProhibitionContentRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Prohibition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

@ExtendWith(MockitoExtension.class)
class ProhibitionContentServiceTest {

  @Mock
  private ProhibitionContentRepository prohibitionContentRepository;

  @InjectMocks
  private ProhibitionContentService prohibitionContentService;

  @Test
  @SneakyThrows
  void shouldReturnProhibitionDescriptionBasedOnTradeType() {
    // given
    Locale en = Locale.EN;
    List<Measure> measures =
        List.of(
            Measure.builder()
                .measureType(
                    MeasureType.builder()
                        .id("730")
                        .seriesId("A")
                        .description("Import control of organic products")
                        .build())
                .measureConditions(List.of())
                .legalActId("A1907950")
                .applicableTradeTypes(List.of(TradeType.IMPORT))
                .build(),
            Measure.builder()
                .measureType(
                    MeasureType.builder()
                        .id("730")
                        .seriesId("A")
                        .description("Import control of organic products")
                        .build())
                .measureConditions(List.of())
                .legalActId("A1907951")
                .applicableTradeTypes(List.of(TradeType.EXPORT))
                .build());
    String exportsDescription = "Prohibitions and restrictions enforced by customs on goods custom - exports";
    String importsDescription = "Prohibitions and restrictions enforced by customs on goods - imports";
    String description = "Prohibitions and restrictions enforced by customs on goods";
    when(prohibitionContentRepository.findAll())
        .thenReturn(
            Flux.just(
                ProhibitionDescription.builder()
                    .applicableTradeTypes(List.of(TradeType.EXPORT))
                    .legalAct("A1907950")
                    .description(exportsDescription)
                    .locale(en)
                    .build(),
                ProhibitionDescription.builder()
                    .applicableTradeTypes(List.of(TradeType.IMPORT))
                    .legalAct("A1907950")
                    .description(importsDescription)
                    .locale(en)
                    .build(),
                ProhibitionDescription.builder()
                    .applicableTradeTypes(List.of(TradeType.IMPORT, TradeType.EXPORT))
                    .legalAct("A1907951")
                    .description(description)
                    .locale(en)
                    .build()));
    prohibitionContentService.initializeProhibitionContent();

    // when
    Mono<List<Prohibition>> prohibitionsMono =
        prohibitionContentService.getProhibitions(measures, "TR", en, TradeType.IMPORT);

    // then
    StepVerifier.create(prohibitionsMono)
        .expectNextMatches(
            prohibitions -> {
              assertThat(prohibitions).isNotEmpty();
              assertThat(prohibitions).hasSize(2);
              assertThat(prohibitions)
                  .containsExactlyInAnyOrder(
                      Prohibition.builder()
                          .description(importsDescription)
                          .legalAct("A1907950")
                          .id("730")
                          .measureTypeId("730")
                          .measureTypeSeries("A")
                          .measureType(PROHIBITIVE)
                          .build(),
                      Prohibition.builder()
                          .description(description)
                          .legalAct("A1907951")
                          .id("730")
                          .measureTypeId("730")
                          .measureTypeSeries("A")
                          .measureType(PROHIBITIVE)
                          .build());
              return true;
            })
        .verifyComplete();

    prohibitionsMono =
        prohibitionContentService.getProhibitions(measures, "TR", en, TradeType.EXPORT);

    // then
    StepVerifier.create(prohibitionsMono)
        .expectNextMatches(
            prohibitions -> {
              assertThat(prohibitions).isNotEmpty();
              assertThat(prohibitions).hasSize(2);
              assertThat(prohibitions)
                  .containsExactlyInAnyOrder(
                      Prohibition.builder()
                          .description(exportsDescription)
                          .legalAct("A1907950")
                          .id("730")
                          .measureTypeId("730")
                          .measureTypeSeries("A")
                          .measureType(PROHIBITIVE)
                          .build(),
                      Prohibition.builder()
                          .description(description)
                          .legalAct("A1907951")
                          .id("730")
                          .measureTypeId("730")
                          .measureTypeSeries("A")
                          .measureType(PROHIBITIVE)
                          .build());
              return true;
            })
        .verifyComplete();
  }

  @Test
  @SneakyThrows
  void shouldReturnCountrySpecificProhibitionDescription() {
    // given
    Locale en = Locale.EN;
    TradeType tradeType = TradeType.IMPORT;
    List<Measure> measures =
      List.of(
        Measure.builder()
          .measureType(
            MeasureType.builder()
              .id("730")
              .seriesId("A")
              .description("Import control of organic products")
              .build())
          .measureConditions(List.of())
          .legalActId("A1907950")
          .applicableTradeTypes(List.of(tradeType))
          .build());
    String description = "Prohibitions and restrictions enforced by customs on goods custom";
    when(prohibitionContentRepository.findAll())
      .thenReturn(
        Flux.just(
          ProhibitionDescription.builder()
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .legalAct("A1907950")
            .originCountry("TR")
            .description(description)
            .locale(en)
            .build(),
          ProhibitionDescription.builder()
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .legalAct("A1907950")
            .description("Prohibitions and restrictions enforced by customs on goods")
            .locale(en)
            .build()));
    prohibitionContentService.initializeProhibitionContent();

    // when
    Mono<List<Prohibition>> prohibitionsMono =
      prohibitionContentService.getProhibitions(measures, "TR", en, tradeType);

    // then
    StepVerifier.create(prohibitionsMono)
        .expectNextMatches(
            prohibitions -> {
              assertThat(prohibitions).isNotEmpty();
              assertThat(prohibitions).hasSize(1);
              assertThat(prohibitions.get(0))
                  .isEqualTo(
                      Prohibition.builder()
                          .description(description)
                          .legalAct("A1907950")
                          .id("730")
                          .measureTypeId("730")
                          .measureTypeSeries("A")
                          .measureType(PROHIBITIVE)
                          .build());
              return true;
            })
        .verifyComplete();
  }

  @Test
  @SneakyThrows
  void shouldReturnLocaleAndCountrySpecificProhibitionDescription() {
    // given
    Locale cy = Locale.CY;

    TradeType tradeType = TradeType.IMPORT;
    List<Measure> measures =
      List.of(
        Measure.builder()
          .measureType(
            MeasureType.builder()
              .id("730")
              .seriesId("A")
              .description("Import control of organic products")
              .build())
          .measureConditions(List.of())
          .legalActId("A1907950")
          .applicableTradeTypes(List.of(tradeType))
          .build());
    String description = "Prohibitions and restrictions enforced by customs on goods custom";
    when(prohibitionContentRepository.findAll())
      .thenReturn(
        Flux.just(
          ProhibitionDescription.builder()
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .legalAct("A1907950")
            .originCountry("TR")
            .description("some description")
            .locale(Locale.EN)
            .build(),
          ProhibitionDescription.builder()
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .legalAct("A1907950")
            .originCountry("TR")
            .description(description)
            .locale(Locale.CY)
            .build(),
          ProhibitionDescription.builder()
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .legalAct("A1907950")
            .description("Prohibitions and restrictions enforced by customs on goods")
            .locale(Locale.EN)
            .build()));
    prohibitionContentService.initializeProhibitionContent();

    // when
    Mono<List<Prohibition>> prohibitionsMono =
      prohibitionContentService.getProhibitions(measures, "TR", cy, tradeType);

    // then
    StepVerifier.create(prohibitionsMono)
      .expectNextMatches(
        prohibitions -> {
          assertThat(prohibitions).isNotEmpty();
          assertThat(prohibitions).hasSize(1);
          assertThat(prohibitions.get(0))
              .isEqualTo(
                  Prohibition.builder()
                      .description(description)
                      .legalAct("A1907950")
                      .id("730")
                      .measureTypeId("730")
                      .measureTypeSeries("A")
                      .measureType(PROHIBITIVE)
                      .build());
          return true;
        })
      .verifyComplete();
  }

  @Test
  @SneakyThrows
  void shouldReturnConfiguredDefaultProhibitionDescriptionWhenCountrySpecificIsNotSetup() {
    // given
    Locale en = Locale.EN;

    TradeType tradeType = TradeType.IMPORT;
    List<Measure> measures =
      List.of(
        Measure.builder()
          .measureType(
            MeasureType.builder()
              .id("730")
              .seriesId("A")
              .description("Import control of organic products")
              .build())
          .measureConditions(List.of())
          .legalActId("A1907950")
          .applicableTradeTypes(List.of(tradeType))
          .build());
    String description = "Prohibitions and restrictions enforced by customs on goods";
    when(prohibitionContentRepository.findAll())
      .thenReturn(
        Flux.just(
          ProhibitionDescription.builder()
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .legalAct("A1907950")
            .description(description)
            .locale(en)
            .build()));
    prohibitionContentService.initializeProhibitionContent();

    // when
    Mono<List<Prohibition>> prohibitionsMono =
      prohibitionContentService.getProhibitions(measures, "TR", en, tradeType);

    // then
    StepVerifier.create(prohibitionsMono)
      .expectNextMatches(
        prohibitions -> {
          assertThat(prohibitions).isNotEmpty();
          assertThat(prohibitions).hasSize(1);
          assertThat(prohibitions.get(0))
              .isEqualTo(
                  Prohibition.builder()
                      .description(description)
                      .legalAct("A1907950")
                      .id("730")
                      .measureTypeId("730")
                      .measureTypeSeries("A")
                      .measureType(PROHIBITIVE)
                      .build());
          return true;
        })
      .verifyComplete();
  }

  @Test
  @SneakyThrows
  void
  shouldReturnDefaultProhibitionDescriptionWhenNoDefaultDescriptionIsSetUpForLegalAct() {
    // given
    Locale en = Locale.EN;

    TradeType tradeType = TradeType.IMPORT;
    List<Measure> measures =
      List.of(
        Measure.builder()
          .measureType(
            MeasureType.builder()
              .id("730")
              .seriesId("A")
              .description("Import control of organic products")
              .build())
          .measureConditions(List.of())
          .legalActId("A1907950")
          .applicableTradeTypes(List.of(tradeType))
          .build());
    when(prohibitionContentRepository.findAll())
      .thenReturn(
        Flux.just(
          ProhibitionDescription.builder()
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .legalAct("F1907950")
            .description("a description")
            .locale(en)
            .build()));
    prohibitionContentService.initializeProhibitionContent();

    // when
    Mono<List<Prohibition>> prohibitionsMono =
      prohibitionContentService.getProhibitions(measures, "TR", en, tradeType);

    // then
    StepVerifier.create(prohibitionsMono)
      .expectNextMatches(
        prohibitions -> {
          assertThat(prohibitions).isNotEmpty();
          assertThat(prohibitions).hasSize(1);
          assertThat(prohibitions.get(0))
              .isEqualTo(
                  Prohibition.builder()
                      .description(null)
                      .legalAct("A1907950")
                      .id("730")
                      .measureTypeId("730")
                      .measureTypeSeries("A")
                      .measureType(PROHIBITIVE)
                      .build());
          return true;
        })
      .verifyComplete();
  }

  @Test
  @SneakyThrows
  void shouldReturnDefaultProhibitionDescriptionWhenNoProhibitionsDescriptionAreSetup() {
    // given
    Locale en = Locale.EN;

    TradeType tradeType = TradeType.IMPORT;
    List<Measure> measures =
      List.of(
        Measure.builder()
          .measureType(
            MeasureType.builder()
              .id("730")
              .seriesId("A")
              .description("Import control of organic products")
              .build())
          .measureConditions(List.of())
          .legalActId("A1907950")
          .applicableTradeTypes(List.of(tradeType))
          .build());
    when(prohibitionContentRepository.findAll()).thenReturn(Flux.empty());
    prohibitionContentService.initializeProhibitionContent();

    // when
    Mono<List<Prohibition>> prohibitionsMono =
      prohibitionContentService.getProhibitions(measures, "TR", en, tradeType);

    // then
    StepVerifier.create(prohibitionsMono)
      .expectNextMatches(
        prohibitions -> {
          assertThat(prohibitions).isNotEmpty();
          assertThat(prohibitions).hasSize(1);
          assertThat(prohibitions.get(0))
              .isEqualTo(
                  Prohibition.builder()
                      .description(null)
                      .legalAct("A1907950")
                      .id("730")
                      .measureTypeId("730")
                      .measureTypeSeries("A")
                      .measureType(PROHIBITIVE)
                      .build());
          return true;
        })
      .verifyComplete();
  }

  @Test
  @SneakyThrows
  void shouldReturnProhibitionsForMoreThanOneProhibitedMeasures() {
    // given
    Locale en = Locale.EN;

    String measureTypeForA1907950 = "730";
    String legalActA1907950 = "A1907950";
    String descriptionA1907950 =
      "A1907950 Prohibitions and restrictions enforced by customs on goods";

    String measureTypeForF1907950 = "430";
    String legalActF1907950 = "F1907950";
    String descriptionF1907950 =
      "F1907950 Prohibitions and restrictions enforced by customs on goods";

    TradeType tradeType = TradeType.IMPORT;
    List<Measure> measures =
      List.of(
        Measure.builder()
          .measureType(
            MeasureType.builder()
              .id(measureTypeForA1907950)
              .seriesId("A")
              .description("Import control of organic products")
              .build())
          .measureConditions(List.of())
          .legalActId(legalActA1907950)
          .applicableTradeTypes(List.of(tradeType))
          .build(),
        Measure.builder()
          .measureType(
            MeasureType.builder()
              .id(measureTypeForF1907950)
              .seriesId("A")
              .description("Import control of organic products")
              .build())
          .measureConditions(List.of())
          .legalActId(legalActF1907950)
          .applicableTradeTypes(List.of(tradeType))
          .build());
    when(prohibitionContentRepository.findAll())
      .thenReturn(
        Flux.just(
          ProhibitionDescription.builder()
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .legalAct(legalActA1907950)
            .description(descriptionA1907950)
            .locale(en)
            .build(),
          ProhibitionDescription.builder()
            .applicableTradeTypes(List.of(TradeType.IMPORT))
            .legalAct(legalActF1907950)
            .description(descriptionF1907950)
            .locale(en)
            .build()));
    prohibitionContentService.initializeProhibitionContent();

    // when
    Mono<List<Prohibition>> prohibitionsMono =
      prohibitionContentService.getProhibitions(measures, "TR", en, tradeType);

    // then
    StepVerifier.create(prohibitionsMono)
      .expectNextMatches(
        prohibitions -> {
          assertThat(prohibitions).isNotEmpty();
          assertThat(prohibitions).hasSize(2);
          assertThat(prohibitions).containsExactlyInAnyOrder(
              Prohibition.builder()
                      .description(descriptionA1907950)
                      .legalAct(legalActA1907950)
                      .id(measureTypeForA1907950)
                      .measureTypeId(measureTypeForA1907950)
                      .measureTypeSeries("A")
                      .measureType(PROHIBITIVE)
                      .build(),
              Prohibition.builder()
                      .description(descriptionF1907950)
                      .legalAct(legalActF1907950)
                      .id(measureTypeForF1907950)
                      .measureTypeId(measureTypeForF1907950)
                      .measureTypeSeries("A")
                      .measureType(PROHIBITIVE)
                      .build());
          return true;
        })
      .verifyComplete();
  }
}
