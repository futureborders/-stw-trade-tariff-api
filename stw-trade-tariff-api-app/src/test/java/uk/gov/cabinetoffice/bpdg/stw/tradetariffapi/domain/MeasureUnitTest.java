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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureUnit.Type;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureUnit.Type.*;

class MeasureUnitTest {

  private static Stream<Arguments> measureUnitToEnglishVerb() {
    return Stream.of(
        Arguments.of("ASV", "has a percentage by volume of"),
        Arguments.of("CCT", "has a carrying capacity of"),
        Arguments.of("CEN", "consists of"),
        Arguments.of("CTM", "boasts"),
        Arguments.of("DAP", "weigh"),
        Arguments.of("DHS", "weigh"),
        Arguments.of("DTN", "weigh"),
        Arguments.of("EUR", "is valued at"),
        Arguments.of("GFI", "weigh"),
        Arguments.of("GP1", "is produced in"),
        Arguments.of("GRM", "weigh"),
        Arguments.of("GRT", "weigh"),
        Arguments.of("HLT", "is"),
        Arguments.of("HMT", "measures"),
        Arguments.of("KAC", "weigh"),
        Arguments.of("KCC", "weigh"),
        Arguments.of("KCL", "weigh"),
        Arguments.of("KGM", "weigh"),
        Arguments.of("NO_UNIT_CODE", "the unit is"),
        Arguments.of("KLT", "is"),
        Arguments.of("KMA", "weigh"),
        Arguments.of("KMT", "measures"),
        Arguments.of("KNI", "weigh"),
        Arguments.of("KNS", "weigh"),
        Arguments.of("KPH", "weigh"),
        Arguments.of("KPO", "weigh"),
        Arguments.of("KPP", "weigh"),
        Arguments.of("KSD", "weigh"),
        Arguments.of("KSH", "weigh"),
        Arguments.of("KUR", "weigh"),
        Arguments.of("LPA", "is"),
        Arguments.of("LTR", "is"),
        Arguments.of("MIL", "consists of"),
        Arguments.of("MPR", "consists of"),
        Arguments.of("MTK", "measures"),
        Arguments.of("MTQ", "measures"),
        Arguments.of("MTR", "measures"),
        Arguments.of("MWH", "has a kilowatt hours value of"),
        Arguments.of("NAR", "consists of"),
        Arguments.of("NCL", "consists of"),
        Arguments.of("NPR", "consists of"),
        Arguments.of("TJO", "has a gross calorific value of"),
        Arguments.of("TNE", "weigh"),
        Arguments.of("WAT", "has a wattage of"));
  }

  private static Stream<Arguments> measureUnitToSingularUnit() {
    return Stream.of(
        Arguments.of("ASV", "%"),
        Arguments.of("CCT", "metric tonne"),
        Arguments.of("CEN", "00 items"),
        Arguments.of("CTM", "carat (one metric carat = 2 x 10$-$4kg)"),
        Arguments.of("DAP", "decatonne (10 tonnes), corrected according to polarisation"),
        Arguments.of("DHS", "kilogram of dihydrostreptomycin"),
        Arguments.of("DTN", "hectokilogram (100 kilograms)"),
        Arguments.of("EUR", "Euro"),
        Arguments.of("GFI", "gram of fissile isotopes"),
        Arguments.of("GP1", "unit"),
        Arguments.of("GRM", "gram"),
        Arguments.of("GRT", "gross tonnage"),
        Arguments.of("HLT", "hectolitre (100 litres)"),
        Arguments.of("HMT", "hectometre (100 metres)"),
        Arguments.of("KAC", "kilogram net of acesulfame potassium"),
        Arguments.of("KCC", "kilogram of choline chloride"),
        Arguments.of("KCL", "tonne of potassium chloride"),
        Arguments.of("KGM", "kilogram"),
        Arguments.of("NO_UNIT_CODE", ""),
        Arguments.of("KLT", "kilolitre (1000 litres)"),
        Arguments.of("KMA", "kilogram of methylamines"),
        Arguments.of("KMT", "kilometre"),
        Arguments.of("KNI", "kilogram of nitrogen"),
        Arguments.of("KNS", "kilogram of hydrogen peroxide"),
        Arguments.of("KPH", "kilogram of potassium hydroxide"),
        Arguments.of("KPO", "kilogram of potassium oxide"),
        Arguments.of("KPP", "kilogram of diphosphorus pentaoxide"),
        Arguments.of("KSD", "kilogram of substance 90% dry"),
        Arguments.of("KSH", "kilogram of sodium hydroxide"),
        Arguments.of("KUR", "kilogram of uranium"),
        Arguments.of("LPA", "litre of pure alcohol"),
        Arguments.of("LTR", "litre"),
        Arguments.of("MIL", "000 items"),
        Arguments.of("MPR", "000 pairs"),
        Arguments.of("MTK", "square metre"),
        Arguments.of("MTQ", "cubic metre"),
        Arguments.of("MTR", "metre"),
        Arguments.of("MWH", "000 kilowatt hours"),
        Arguments.of("NAR", "item"),
        Arguments.of("NCL", "cell"),
        Arguments.of("NPR", "pair"),
        Arguments.of("TJO", "terajoule"),
        Arguments.of("TNE", "tonne"),
        Arguments.of("WAT", "watt"));
  }

  private static Stream<Arguments> measureUnitToPluralUnit() {
    return Stream.of(
        Arguments.of("ASV", "%"),
        Arguments.of("CCT", "metric tonnes"),
        Arguments.of("CEN", "00 items"),
        Arguments.of("CTM", "carats (one metric carat = 2 x 10$-$4kg)"),
        Arguments.of("DAP", "decatonnes (10 tonnes), corrected according to polarisation"),
        Arguments.of("DHS", "kilograms of dihydrostreptomycin"),
        Arguments.of("DTN", "hectokilograms (100 kilograms)"),
        Arguments.of("EUR", "Euros"),
        Arguments.of("GFI", "grams of fissile isotopes"),
        Arguments.of("GP1", "units"),
        Arguments.of("GRM", "grams"),
        Arguments.of("GRT", "gross tonnage"),
        Arguments.of("HLT", "hectolitres (100 litres)"),
        Arguments.of("HMT", "hectometres (100 metres)"),
        Arguments.of("KAC", "kilograms net of acesulfame potassium"),
        Arguments.of("KCC", "kilograms of choline chloride"),
        Arguments.of("KCL", "tonnes of potassium chloride"),
        Arguments.of("KGM", "kilograms"),
        Arguments.of("NO_UNIT_CODE", ""),
        Arguments.of("KLT", "kilolitres (1000 litres)"),
        Arguments.of("KMA", "kilograms of methylamines"),
        Arguments.of("KMT", "kilometres"),
        Arguments.of("KNI", "kilograms of nitrogen"),
        Arguments.of("KNS", "kilograms of hydrogen peroxide"),
        Arguments.of("KPH", "kilograms of potassium hydroxide"),
        Arguments.of("KPO", "kilograms of potassium oxide"),
        Arguments.of("KPP", "kilograms of diphosphorus pentaoxide"),
        Arguments.of("KSD", "kilograms of substance 90% dry"),
        Arguments.of("KSH", "kilograms of sodium hydroxide"),
        Arguments.of("KUR", "kilograms of uranium"),
        Arguments.of("LPA", "litres of pure alcohol"),
        Arguments.of("LTR", "litres"),
        Arguments.of("MIL", "000 items"),
        Arguments.of("MPR", "000 pairs"),
        Arguments.of("MTK", "square metres"),
        Arguments.of("MTQ", "cubic metres"),
        Arguments.of("MTR", "metres"),
        Arguments.of("MWH", "000 kilowatt hours"),
        Arguments.of("NAR", "items"),
        Arguments.of("NCL", "cells"),
        Arguments.of("NPR", "pairs"),
        Arguments.of("TJO", "terajoules"),
        Arguments.of("TNE", "tonnes"),
        Arguments.of("WAT", "watts"));
  }

  private static Stream<Arguments> measureUnitToType() {
    return Stream.of(
        Arguments.of("ASV", UNIT),
        Arguments.of("CCT", UNIT),
        Arguments.of("CEN", UNIT),
        Arguments.of("CTM", UNIT),
        Arguments.of("DAP", WEIGHT),
        Arguments.of("DHS", WEIGHT),
        Arguments.of("DTN", WEIGHT),
        Arguments.of("EUR", UNIT),
        Arguments.of("GFI", WEIGHT),
        Arguments.of("GP1", UNIT),
        Arguments.of("GRM", WEIGHT),
        Arguments.of("GRT", WEIGHT),
        Arguments.of("HLT", VOLUME),
        Arguments.of("HMT", UNIT),
        Arguments.of("KAC", WEIGHT),
        Arguments.of("KCC", WEIGHT),
        Arguments.of("KCL", WEIGHT),
        Arguments.of("KGM", WEIGHT),
        Arguments.of("NO_UNIT_CODE", UNIT),
        Arguments.of("KLT", VOLUME),
        Arguments.of("KMA", WEIGHT),
        Arguments.of("KMT", UNIT),
        Arguments.of("KNI", WEIGHT),
        Arguments.of("KNS", WEIGHT),
        Arguments.of("KPH", WEIGHT),
        Arguments.of("KPO", WEIGHT),
        Arguments.of("KPP", WEIGHT),
        Arguments.of("KSD", WEIGHT),
        Arguments.of("KSH", WEIGHT),
        Arguments.of("KUR", WEIGHT),
        Arguments.of("LPA", VOLUME),
        Arguments.of("LTR", VOLUME),
        Arguments.of("MIL", UNIT),
        Arguments.of("MPR", UNIT),
        Arguments.of("MTK", UNIT),
        Arguments.of("MTQ", UNIT),
        Arguments.of("MTR", UNIT),
        Arguments.of("MWH", UNIT),
        Arguments.of("NAR", UNIT),
        Arguments.of("NCL", UNIT),
        Arguments.of("NPR", UNIT),
        Arguments.of("TJO", UNIT),
        Arguments.of("TNE", WEIGHT),
        Arguments.of("WAT", UNIT));
  }

  @ParameterizedTest
  @MethodSource("measureUnitToEnglishVerb")
  void shouldGetEnglishVerb(String measureUnit, String englishVerb) {
    assertThat(MeasureUnit.valueOf(measureUnit).getVerb(Locale.EN)).isEqualTo(englishVerb);
  }

  @ParameterizedTest
  @MethodSource("measureUnitToSingularUnit")
  void shouldGetSingularUnit(String measureUnit, String singularUnit) {
    assertThat(MeasureUnit.valueOf(measureUnit).getUnit(Locale.EN, 1)).isEqualTo(singularUnit);
  }

  @ParameterizedTest
  @MethodSource("measureUnitToPluralUnit")
  void shouldGetPluralUnit(String measureUnit, String pluralUnit) {
    assertThat(MeasureUnit.valueOf(measureUnit).getUnit(Locale.EN, 2)).isEqualTo(pluralUnit);
  }

  @ParameterizedTest
  @MethodSource("measureUnitToType")
  void shouldRecogniseCorrectType(String measureUnit, Type type) {
    assertThat(MeasureUnit.valueOf(measureUnit).isUnitBased()).isEqualTo(type == UNIT);
    assertThat(MeasureUnit.valueOf(measureUnit).isWeightBased()).isEqualTo(type == WEIGHT);
    assertThat(MeasureUnit.valueOf(measureUnit).isVolumeBased()).isEqualTo(type == VOLUME);
  }
}
