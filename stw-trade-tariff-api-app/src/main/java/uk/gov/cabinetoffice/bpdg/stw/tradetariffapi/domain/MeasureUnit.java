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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum MeasureUnit {
  ASV("%vol", "has a percentage by volume of", "%", "tbc", "tbc"),
  CCT(
    "Carrying capacity in metric tonnes",
    "has a carrying capacity of",
    "metric tonnes",
    "tbc",
    "tbc"),
  CEN("Hundred items", "consists of", "00 items", "tbc", "tbc"),
  CTM(
    "Carats (one metric carat = 2 x 10$-$4kg)",
    "boasts",
    "carats (one metric carat = 2 x 10$-$4kg)",
    "tbc",
    "tbc"),
  DAP(
    "Decatonne, corrected according to polarisation",
    "weighs",
    "decatonnes (10 tonnes), corrected according to polarisation",
    "tbc",
    "tbc"),
  DHS(
    "Kilogram of dihydrostreptomycin",
    "weighs",
    "Kilograms of dihydrostreptomycin",
    "tbc",
    "tbc"),
  DTN("Hectokilogram", "weighs", "hectokilograms (100 kilograms)", "tbc", "tbc"),
  EUR("Euro (used for statistical surveillance)", "is valued at", "Euros", "tbc", "tbc"),
  GFI("Gram of fissile isotopes", "weighs", "grammes of fissile isotopes", "tbc", "tbc"),
  GP1("Gross Production", "is produced in", "units", "tbc", "tbc"),
  GRM("Gram", "weighs", "grammes", "tbc", "tbc"),
  GRT("Gross tonnage", "weighs", "gross tonnage", "tbc", "tbc"),
  HLT("Hectolitre", "is", "hectolitres (100 litres)", "tbc", "tbc"),
  HMT("Hectometre", "measures", "hectometres (100 metres)", "tbc", "tbc"),
  KAC(
    "Kilogram net of acesulfame potassium",
    "weighs",
    "kilograms net of acesulfame potassium",
    "tbc",
    "tbc"),
  KCC("Kilogram of choline chloride", "weighs", "kilograms of choline chloride", "tbc", "tbc"),
  KCL("Tonne of potassium chloride", "weighs", "tonnes of potassium chloride", "tbc", "tbc"),
  KGM("Kilogram", "weighs", "kilograms", "tbc", "tbc"),
  KLT("1000 litres", "is", "kilolitres (1000 litres)", "tbc", "tbc"),
  KMA("Kilogram of methylamines", "weighs", "kilograms of methylamines", "tbc", "tbc"),
  KMT("Kilometre", "measures", "kilometres", "tbc", "tbc"),
  KNI("Kilogram of nitrogen", "weighs", "kilograms of nitrogen", "tbc", "tbc"),
  KNS("Kilogram of hydrogen peroxide", "weighs", "kilograms of hydrogen peroxide", "tbc", "tbc"),
  KPH(
    "Kilogram of potassium hydroxide (caustic potash)",
    "weighs",
    "kilograms of potassium hydroxide",
    "tbc",
    "tbc"),
  KPO("Kilogram of potassium oxide", "weighs", "kilograms of potassium oxide", "tbc", "tbc"),
  KPP(
    "Kilogram of diphosphorus pentaoxide",
    "weighs",
    "kilograms of diphosphorus pentaoxide",
    "tbc",
    "tbc"),
  KSD("Kilogram of substance 90 % dry", "weighs", "kilograms of substance 90% dry", "tbc", "tbc"),
  KSH(
    "Kilogram of sodium hydroxide (caustic soda)",
    "weighs",
    "kilograms of sodium hydroxide",
    "tbc",
    "tbc"),
  KUR("Kilogram of uranium", "weighs", "kilograms of uranium", "tbc", "tbc"),
  LPA("Litre pure (100%) alcohol", "is", "litres of pure alcohol", "tbc", "tbc"),
  LTR("Litre", "is", "litres", "tbc", "tbc"),
  MIL("1000 items", "consists of", "000 items", "tbc", "tbc"),
  MPR("1000 pairs (used for statistical surveillance)", "consists of", "000 pairs", "tbc", "tbc"),
  MTK("Square metre", "measures", "square metres", "tbc", "tbc"),
  MTQ("Cubic meter", "measures", "cubic metres", "tbc", "tbc"),
  MTR("Metre", "measures", "metres", "tbc", "tbc"),
  MWH("1000 kilowatt hours", "has a kilowatt hours value of", "000 kilowatt hours", "tbc", "tbc"),
  NAR("Number of items", "consists of", "items", "tbc", "tbc"),
  NCL("Number of cells", "consists of", "cells", "tbc", "tbc"),
  NPR("Number of pairs", "consists of", "pairs", "tbc", "tbc"),
  TJO(
    "Terajoule (gross calorific value)",
    "has a gross calorific value of",
    "terajoules",
    "tbc",
    "tbc"),
  TNE("Tonne", "weighs", "tonnes", "tbc", "tbc"),
  WAT("Number of Watt", "has a wattage of", "watts", "tbc", "tbc");

  private static Map<String, MeasureUnit> measureUnitMap =
    Collections.unmodifiableMap(initialiseMeasureUnitMap());

  private static Map<String, MeasureUnit> initialiseMeasureUnitMap() {
    Map<String, MeasureUnit> measureUnitMap = new HashMap<>();
    for (MeasureUnit measureUnit : MeasureUnit.values()) {
      measureUnitMap.put(measureUnit.tradeTariffAbbr, measureUnit);
    }
    return measureUnitMap;
  }

  MeasureUnit(
    String tradeTariffAbbr,
    String englishVerb,
    String englishUnit,
    String welshVerb,
    String welshUnit) {
    this.tradeTariffAbbr = tradeTariffAbbr;
    this.englishVerb = englishVerb;
    this.englishUnit = englishUnit;
    this.welshVerb = welshVerb;
    this.welshUnit = welshUnit;
  }

  private final String tradeTariffAbbr;
  private final String englishVerb;
  private final String welshVerb;
  private final String englishUnit;
  private final String welshUnit;

  public static MeasureUnit getMeasureUnit(String tradeTariffAbbr) {
    return Optional.ofNullable(measureUnitMap.get(tradeTariffAbbr))
      .orElseThrow(
        () ->
          new IllegalArgumentException(
            String.format("Measure unit '%s' not handled", tradeTariffAbbr)));
  }

  public String getVerb(Locale locale) {
    return locale == Locale.EN ? englishVerb : welshVerb;
  }

  public String getUnit(Locale locale) {
    return locale == Locale.EN ? englishUnit : welshUnit;
  }
}
