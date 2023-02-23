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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.utils;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureCondition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionCode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MeasureConditionsTestUtils {

  public static List<MeasureCondition> stubMeasureConditions() {
    MeasureConditionCode conditionCodeC = MeasureConditionCode.C;
    MeasureCondition y926 =
        MeasureCondition.builder()
            .id("1453733")
            .conditionCode(MeasureConditionCode.B)
            .documentCode("Y926")
            .requirement(
                "Particular provisions: Goods not concerned by import prohibition on fluorinated greenhouse gases ")
            .build();
    MeasureCondition c057 =
        MeasureCondition.builder()
            .id("1453735")
            .conditionCode(conditionCodeC)
            .documentCode("C057")
            .requirement(
                "Other certificates: Copy of the declaration of conformity - Option A, as referred to in Article 1.2 and in the Annex of Regulation (EU) 2016/879 ")
            .build();
    MeasureCondition c079 =
        MeasureCondition.builder()
            .id("1453736")
            .conditionCode(conditionCodeC)
            .documentCode("C079")
            .requirement(
                "Other certificates: Copy of the declaration of conformity - Option B, as referred to in Article 1.2 and in the Annex of Regulation (EU) 2016/879 ")
            .build();
    MeasureCondition c082 =
        MeasureCondition.builder()
            .id("1453737")
            .conditionCode(conditionCodeC)
            .documentCode("C082")
            .requirement(
                "Other certificates: Copy of the declaration of conformity - Option C, as referred to in Article 1.2 and in the Annex of Regulation (EU) 2016/879")
            .build();
    MeasureCondition y950 =
        MeasureCondition.builder()
            .id("1453738")
            .conditionCode(conditionCodeC)
            .documentCode("Y950")
            .requirement(
                "Particular provisions: Goods other than pre-charged equipment with hydrofluorocarbons ")
            .build();
    MeasureCondition y951 =
        MeasureCondition.builder()
            .id("1453739")
            .conditionCode(conditionCodeC)
            .documentCode("Y951")
            .requirement(
                "Particular provisions: Exemptions from the Reduction of the quantity of hydrofluorocarbons placed on the market by virtue of Article 15.2 of Regulation (EU) No 517/2014")
            .build();

    return List.of(y926, c057, c079, c082, y950, y951);
  }
}
