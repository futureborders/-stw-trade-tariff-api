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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("lgtm[java/non-static-nested-class]")
public class MeasureUnitTest {

  @Nested
  class GetMeasureUnit {

    @Test
    @DisplayName("happy path")
    void happyPath() {
      assertThat(MeasureUnit.getMeasureUnit("Kilogram")).isEqualTo(MeasureUnit.KGM);
    }

    @Test
    @DisplayName("error scenario")
    void errorScenario() {

      assertThatIllegalArgumentException()
        .isThrownBy(() -> MeasureUnit.getMeasureUnit("unknown"))
        .withMessage("Measure unit 'unknown' not handled");
    }
  }
}
