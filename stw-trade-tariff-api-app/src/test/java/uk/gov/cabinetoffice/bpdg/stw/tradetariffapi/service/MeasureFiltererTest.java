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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.AdditionalCode;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.GeographicalArea;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Measure;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;

public class MeasureFiltererTest {

  private final MeasureFilterer measureFilterer = new MeasureFilterer();

  @Nested
  class FilterRestrictiveMeasures {

    @Test
    @DisplayName(
        "Trade Type as IMPORT and measures are assigned to a group country where there are no countries assigned")
    void shouldIgnoreMeasureWhereGeographicalAreaIsGroupAndNoCountriesUnderThem() {
      Measure geographicalAreaWithNoCountriesMeasures =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder().id("1").seriesId("B").description("Measure Type 1").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("").id("1080").build())
              .build();
      Measure measureRelatedToUSA =
          Measure.builder()
              .id("2")
              .measureType(
                  MeasureType.builder().id("2").seriesId("B").description("Measure Type 2").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("United States of America")
                      .id("US")
                      .build())
              .build();

      List<Measure> measureResponse =
          List.of(geographicalAreaWithNoCountriesMeasures, measureRelatedToUSA);

      List<Measure> filteredMeasures =
          measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.IMPORT, "US");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(measureRelatedToUSA);
    }

    @Test
    @DisplayName("Trade Type as IMPORT and measures are assigned to a single country")
    void importSingleCountry() {
      Measure measureRelatedToChina =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder().id("1").seriesId("B").description("Measure Type 1").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();
      Measure measureRelatedToUSA =
          Measure.builder()
              .id("2")
              .measureType(
                  MeasureType.builder().id("2").seriesId("B").description("Measure Type 2").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("United States of America")
                      .id("US")
                      .build())
              .build();

      List<Measure> measureResponse = List.of(measureRelatedToChina, measureRelatedToUSA);

      List<Measure> filteredMeasures =
          measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.IMPORT, "CN");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(measureRelatedToChina);
    }

    @Test
    @DisplayName("Measures applicable to both trade types")
    void shouldDealWithMessagesWhichAreApplicableForTheTradeType() {
      Measure measureRelatedToExportOnly =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder().id("1").seriesId("B").description("Measure Type 1").build())
              .applicableTradeTypes(List.of(TradeType.EXPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();
      Measure measureRelatedToImportAndExport =
          Measure.builder()
              .id("2")
              .measureType(
                  MeasureType.builder().id("2").seriesId("B").description("Measure Type 2").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT, TradeType.EXPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();
      Measure measureRelatedToImportsOnly =
          Measure.builder()
              .id("3")
              .measureType(
                  MeasureType.builder().id("3").seriesId("B").description("Measure Type 3").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();

      List<Measure> measureResponse =
          List.of(
              measureRelatedToExportOnly,
              measureRelatedToImportAndExport,
              measureRelatedToImportsOnly);

      List<Measure> filteredMeasures =
          measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.IMPORT, "CN");
      assertThat(filteredMeasures)
          .containsExactlyInAnyOrder(measureRelatedToImportAndExport, measureRelatedToImportsOnly);

      filteredMeasures =
          measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.EXPORT, "CN");
      assertThat(filteredMeasures)
          .containsExactlyInAnyOrder(measureRelatedToImportAndExport, measureRelatedToExportOnly);
    }

    @Test
    @DisplayName(
        "Trade Type as EXPORT and measures are assigned to a single country and global area (ERGA OMNES)")
    void export_single_country_and_global_area() {

      Measure measureRelatedToChina =
          Measure.builder()
              .id("1")
              .applicableTradeTypes(List.of(TradeType.EXPORT))
              .measureType(
                  MeasureType.builder().id("1").seriesId("B").description("Measure Type 1").build())
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();
      Measure measureRelatedToErgaOmnes =
          Measure.builder()
              .id("2")
              .applicableTradeTypes(List.of(TradeType.EXPORT))
              .measureType(
                  MeasureType.builder().id("2").seriesId("B").description("Measure Type 2").build())
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("ERGA OMNES")
                      .id("1011")
                      .childrenGeographicalAreas(Set.of("CN", "US"))
                      .build())
              .build();

      List<Measure> measureResponse = List.of(measureRelatedToChina, measureRelatedToErgaOmnes);

      List<Measure> filteredMeasures =
          measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.EXPORT, "CN");
      assertThat(filteredMeasures)
          .containsExactlyInAnyOrder(measureRelatedToChina, measureRelatedToErgaOmnes);
    }

    @Test
    @DisplayName("should return measure related to ERGA OMNES for EU country")
    void should_return_measure_any_country_erga_omnes() {
      Measure measureRelatedToErgaOmnes =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder().id("1").seriesId("B").description("Measure Type 1").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("ERGA OMNES")
                      .id("1011")
                      .childrenGeographicalAreas(Set.of("EU"))
                      .build())
              .build();

      List<Measure> filteredMeasures =
          measureFilterer.getRestrictiveMeasures(
              List.of(measureRelatedToErgaOmnes), TradeType.IMPORT, "FR");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(measureRelatedToErgaOmnes);
    }

    @Test
    @DisplayName("Should return only the measure to the most specific geographical area")
    void shouldReturnTheMeasureToTheMostSpecificGeographicalArea() {
      Measure measureRelatedToChina =
          Measure.builder()
              .id("1")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                  MeasureType.builder().id("1").seriesId("B").description("Measure Type 1").build())
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();
      Measure measureRelatedToErgaOmnes =
          Measure.builder()
              .id("2")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                  MeasureType.builder().id("1").seriesId("B").description("Measure Type 1").build())
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("ERGA OMNES")
                      .id("1011")
                      .childrenGeographicalAreas(Set.of("CN", "US"))
                      .build())
              .build();

      List<Measure> measureResponse = List.of(measureRelatedToChina, measureRelatedToErgaOmnes);

      List<Measure> filteredMeasures =
          measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.IMPORT, "CN");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(measureRelatedToChina);
    }

    @Test
    @DisplayName("Should not return measures if the given country is excluded")
    void shouldNotReturnExcludedCountry() {
      Measure measureRelatedToChina =
          Measure.builder()
              .id("1")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                  MeasureType.builder().id("1").seriesId("B").description("Measure Type 1").build())
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();
      Measure measureRelatedToErgaOmnes =
          Measure.builder()
              .id("2")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                  MeasureType.builder().id("2").seriesId("B").description("Measure Type 2").build())
              .excludedCountries(Set.of("CN"))
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("ERGA OMNES")
                      .id("1011")
                      .childrenGeographicalAreas(Set.of("CN", "US"))
                      .build())
              .build();

      List<Measure> measureResponse = List.of(measureRelatedToChina, measureRelatedToErgaOmnes);

      List<Measure> filteredMeasures =
          measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.IMPORT, "CN");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(measureRelatedToChina);
    }

    @Test
    @DisplayName("Any Measures with a disallowed series ID should not be returned")
    void shouldExcludeDisallowedSeriesId() {
      Measure seriesIdCMeasure =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder().id("1").seriesId("C").description("Measure Type 1").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();
      Measure seriesIdAMeasure =
          Measure.builder()
              .id("2")
              .measureType(
                  MeasureType.builder().id("2").seriesId("A").description("Measure Type 2").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();

      List<Measure> measureResponse = List.of(seriesIdCMeasure, seriesIdAMeasure);

      List<Measure> filteredMeasures =
          measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.IMPORT, "CN");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(seriesIdAMeasure);
    }

    @ParameterizedTest
    @ValueSource(strings = {"464", "481", "482", "483", "484", "495", "496", "730"})
    void shouldExcludeDisallowedMeasureId(String disallowedMeasureId) {
      Measure measureWithDisallowedId =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder()
                      .id(disallowedMeasureId)
                      .seriesId("A")
                      .description("Measure Type 1")
                      .build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();

      Measure seriesIdAMeasure =
          Measure.builder()
              .id("3")
              .measureType(
                  MeasureType.builder().id("3").seriesId("A").description("Measure Type 3").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();

      List<Measure> measureResponse = List.of(measureWithDisallowedId, seriesIdAMeasure);

      List<Measure> filteredMeasures =
          measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.IMPORT, "CN");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(seriesIdAMeasure);
    }
  }

  @Nested
  class FilterTaxesMeasures {

    @Test
    @DisplayName("Trade Type as IMPORT and measures are assigned to a single country")
    void importSingleCountry() {
      Measure measureRelatedToChina =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder().id("1").seriesId("C").description("Measure Type 1").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();
      Measure measureRelatedToUSA =
          Measure.builder()
              .id("2")
              .measureType(
                  MeasureType.builder().id("2").seriesId("D").description("Measure Type 2").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("United States of America")
                      .id("US")
                      .build())
              .build();

      List<Measure> measureResponse = List.of(measureRelatedToChina, measureRelatedToUSA);

      List<Measure> filteredMeasures =
          measureFilterer.getTaxAndDutyMeasures(measureResponse, TradeType.IMPORT, "CN");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(measureRelatedToChina);
    }

    @Test
    @DisplayName(
        "Trade Type as EXPORT and measures are assigned to a single country and global area (ERGA OMNES)")
    void export_single_country_and_global_area() {

      Measure measureRelatedToChina =
          Measure.builder()
              .id("1")
              .applicableTradeTypes(List.of(TradeType.EXPORT))
              .measureType(
                  MeasureType.builder().id("1").seriesId("C").description("Measure Type 1").build())
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();
      Measure measureRelatedToErgaOmnes =
          Measure.builder()
              .id("2")
              .applicableTradeTypes(List.of(TradeType.EXPORT))
              .measureType(
                  MeasureType.builder().id("2").seriesId("D").description("Measure Type 2").build())
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("ERGA OMNES")
                      .id("1011")
                      .childrenGeographicalAreas(Set.of("CN", "US"))
                      .build())
              .build();

      List<Measure> measureResponse = List.of(measureRelatedToChina, measureRelatedToErgaOmnes);

      List<Measure> filteredMeasures =
          measureFilterer.getTaxAndDutyMeasures(measureResponse, TradeType.EXPORT, "CN");
      assertThat(filteredMeasures)
          .containsExactlyInAnyOrder(measureRelatedToChina, measureRelatedToErgaOmnes);
    }

    @Test
    @DisplayName("should return measure related to ERGA OMNES for EU country")
    void shouldReturnMeasureAssignedToAnyCountryErgaOmnes() {
      Measure measureRelatedToErgaOmnes =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder().id("1").seriesId("C").description("Measure Type 1").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("ERGA OMNES")
                      .id("1011")
                      .childrenGeographicalAreas(Set.of("EU"))
                      .build())
              .build();

      List<Measure> filteredMeasures =
          measureFilterer.getTaxAndDutyMeasures(
              List.of(measureRelatedToErgaOmnes), TradeType.IMPORT, "FR");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(measureRelatedToErgaOmnes);
    }

    @Test
    @DisplayName(
        "Should return the measures for a specific geographical area as well as measures applicable to a country group if that specific geographical area is also a part of the group")
    void shouldReturnTheMeasureToTheMostSpecificGeographicalArea() {
      Measure measureRelatedToChina =
          Measure.builder()
              .id("1")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                  MeasureType.builder().id("1").seriesId("C").description("Measure Type 1").build())
              .geographicalArea(GeographicalArea.builder().description("China").id("KE").build())
              .build();
      Measure measureRelatedToGSP =
          Measure.builder()
              .id("2")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                  MeasureType.builder().id("2").seriesId("D").description("Measure Type 2").build())
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("GSP Countries")
                      .id("1020")
                      .childrenGeographicalAreas(Set.of("KE", "US"))
                      .build())
              .build();
      Measure measureRelatedToSomeOtherCountries =
          Measure.builder()
              .id("3")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                  MeasureType.builder().id("3").seriesId("J").description("Measure Type 3").build())
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("Other group of countries")
                      .id("1021")
                      .childrenGeographicalAreas(Set.of("CH", "US"))
                      .build())
              .build();

      List<Measure> filteredMeasures =
          measureFilterer.getTaxAndDutyMeasures(
              List.of(
                  measureRelatedToChina, measureRelatedToGSP, measureRelatedToSomeOtherCountries),
              TradeType.IMPORT,
              "KE");
      assertThat(filteredMeasures)
          .containsExactlyInAnyOrder(measureRelatedToChina, measureRelatedToGSP);
    }

    @Test
    @DisplayName("Should not return measures if the given country is excluded")
    void shouldNotReturnExcludedCountry() {
      Measure measureRelatedToChina =
          Measure.builder()
              .id("1")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                  MeasureType.builder().id("1").seriesId("C").description("Measure Type 1").build())
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();
      Measure measureRelatedToErgaOmnes =
          Measure.builder()
              .id("2")
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .measureType(
                  MeasureType.builder().id("2").seriesId("D").description("Measure Type 2").build())
              .excludedCountries(Set.of("CN"))
              .geographicalArea(
                  GeographicalArea.builder()
                      .description("ERGA OMNES")
                      .id("1011")
                      .childrenGeographicalAreas(Set.of("CN", "US"))
                      .build())
              .build();

      List<Measure> filteredMeasures =
          measureFilterer.getTaxAndDutyMeasures(
              List.of(measureRelatedToChina, measureRelatedToErgaOmnes), TradeType.IMPORT, "CN");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(measureRelatedToChina);
    }

    @ParameterizedTest
    @CsvSource({"C", "D", "J", "P", "Q"})
    @DisplayName("Measures with certain series id should be included")
    void shouldIncludeMeasuresWithTaxRelatedSeriesId(String seriesId) {
      Measure seriesIdMeasure =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder()
                      .id("1")
                      .seriesId(seriesId)
                      .description("Measure Type 1")
                      .build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();

      List<Measure> measureResponse = List.of(seriesIdMeasure);

      List<Measure> filteredMeasures =
          measureFilterer.getTaxAndDutyMeasures(measureResponse, TradeType.IMPORT, "CN");
      assertThat(filteredMeasures).containsExactlyInAnyOrder(seriesIdMeasure);
    }

    @ParameterizedTest
    @CsvSource({
      "A", "B", "E", "F", "G", "H", "I", "K", "L", "M", "N", "O", "R", "S", "T", "U", "V", "W", "X",
      "Y", "Z"
    })
    @DisplayName("Measures with series id which are not related to tax should not be included")
    void shouldExcludeMeasuresWhichAreNotTaxRelated(String seriesId) {
      Measure seriesIdMeasure =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder()
                      .id("1")
                      .seriesId(seriesId)
                      .description("Measure Type 1")
                      .build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();

      List<Measure> measureResponse = List.of(seriesIdMeasure);

      List<Measure> filteredMeasures =
          measureFilterer.getTaxAndDutyMeasures(measureResponse, TradeType.IMPORT, "CN");
      assertThat(filteredMeasures).isEmpty();
    }
  }

  @Nested
  class AdditionCodeFiltering {

    @Test
    @DisplayName(
        "When a non-residual additional code is present in request, then of all the Measures with an additional code, only return the one with the matching additional code.")
    void shouldExcludeAllOtherMeasuresWithAdditionalCodesWhenNonResidualAdditionalCodeInRequest() {
      Measure measureWithAdditionalCode1 =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder()
                      .id("475")
                      .seriesId("B")
                      .description("Measure Type 1")
                      .build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .additionalCode(
                  AdditionalCode.builder().code("4200").description("Procyon lotor").build())
              .build();
      Measure measureWithAdditionalCode2 =
          Measure.builder()
              .id("2")
              .measureType(
                  MeasureType.builder()
                      .id("475")
                      .seriesId("B")
                      .description("Measure Type 1")
                      .build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .additionalCode(
                  AdditionalCode.builder().code("4201").description("Canis lupus").build())
              .build();
      Measure measureWithAdditionalCode3 =
          Measure.builder()
              .id("3")
              .measureType(
                  MeasureType.builder()
                      .id("475")
                      .seriesId("B")
                      .description("Measure Type 1")
                      .build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .additionalCode(
                  AdditionalCode.builder().code("4202").description("Martes zibellina").build())
              .build();
      Measure seriesIdAMeasureWithNoAdditionalCode =
          Measure.builder()
              .id("4")
              .measureType(
                  MeasureType.builder().id("2").seriesId("A").description("Measure Type 2").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();

      List<Measure> measureResponse =
          List.of(
              measureWithAdditionalCode1,
              measureWithAdditionalCode2,
              measureWithAdditionalCode3,
              seriesIdAMeasureWithNoAdditionalCode);

      List<Measure> filteredMeasures =
          measureFilterer.maybeFilterByAdditionalCode(
              measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.IMPORT, "CN"),
              Optional.of("4202"));
      assertThat(filteredMeasures)
          .containsExactlyInAnyOrder(
              measureWithAdditionalCode3, seriesIdAMeasureWithNoAdditionalCode);
    }

    @Test
    @DisplayName(
        "When a residual additional code is present in request, then exclude all the Measures with an additional code.")
    void shouldExcludeAllMeasuresWithAdditionalCodesWhenResidualAdditionalCodeInRequest() {
      Measure measureWithAdditionalCode1 =
          Measure.builder()
              .id("1")
              .measureType(
                  MeasureType.builder()
                      .id("475")
                      .seriesId("B")
                      .description("Measure Type 1")
                      .build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .additionalCode(
                  AdditionalCode.builder().code("4200").description("Procyon lotor").build())
              .build();
      Measure measureWithAdditionalCode2 =
          Measure.builder()
              .id("2")
              .measureType(
                  MeasureType.builder()
                      .id("475")
                      .seriesId("B")
                      .description("Measure Type 1")
                      .build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .additionalCode(
                  AdditionalCode.builder().code("4201").description("Canis lupus").build())
              .build();
      Measure measureWithAdditionalCode3 =
          Measure.builder()
              .id("3")
              .measureType(
                  MeasureType.builder()
                      .id("475")
                      .seriesId("B")
                      .description("Measure Type 1")
                      .build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .additionalCode(
                  AdditionalCode.builder().code("4202").description("Martes zibellina").build())
              .build();
      Measure measureWithAdditionalCode4 =
          Measure.builder()
              .id("4")
              .measureType(
                  MeasureType.builder()
                      .id("475")
                      .seriesId("B")
                      .description("Measure Type 1")
                      .build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .additionalCode(AdditionalCode.builder().code("4999").description("Other").build())
              .build();
      Measure seriesIdAMeasureWithNoAdditionalCode =
          Measure.builder()
              .id("5")
              .measureType(
                  MeasureType.builder().id("2").seriesId("A").description("Measure Type 2").build())
              .applicableTradeTypes(List.of(TradeType.IMPORT))
              .geographicalArea(GeographicalArea.builder().description("China").id("CN").build())
              .build();

      List<Measure> measureResponse =
          List.of(
              measureWithAdditionalCode1,
              measureWithAdditionalCode2,
              measureWithAdditionalCode3,
              measureWithAdditionalCode4,
              seriesIdAMeasureWithNoAdditionalCode);

      List<Measure> filteredMeasures =
          measureFilterer.maybeFilterByAdditionalCode(
              measureFilterer.getRestrictiveMeasures(measureResponse, TradeType.IMPORT, "CN"),
              Optional.of("4999"));
      assertThat(filteredMeasures).containsExactlyInAnyOrder(seriesIdAMeasureWithNoAdditionalCode);
    }
  }
}
