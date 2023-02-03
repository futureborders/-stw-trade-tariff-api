/*
 * Copyright 2022 Crown Copyright (Single Trade Window)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MeasureConditionCodeTest {

  private static Stream<Arguments> measureConditionCodesArgument() {
    return Stream.of(
        Arguments.of("A", MeasureConditionCode.A),
        Arguments.of("B", MeasureConditionCode.B),
        Arguments.of("C", MeasureConditionCode.C),
        Arguments.of("D", MeasureConditionCode.D),
        Arguments.of("E", MeasureConditionCode.E),
        Arguments.of("F", MeasureConditionCode.F),
        Arguments.of("G", MeasureConditionCode.G),
        Arguments.of("H", MeasureConditionCode.H),
        Arguments.of("I", MeasureConditionCode.I),
        Arguments.of("J", MeasureConditionCode.J),
        Arguments.of("K", MeasureConditionCode.K),
        Arguments.of("L", MeasureConditionCode.L),
        Arguments.of("M", MeasureConditionCode.M),
        Arguments.of("N", MeasureConditionCode.N),
        Arguments.of("O", MeasureConditionCode.O),
        Arguments.of("P", MeasureConditionCode.P),
        Arguments.of("Q", MeasureConditionCode.Q),
        Arguments.of("R", MeasureConditionCode.R),
        Arguments.of("S", MeasureConditionCode.S),
        Arguments.of("U", MeasureConditionCode.U),
        Arguments.of("V", MeasureConditionCode.V),
        Arguments.of("W", MeasureConditionCode.W),
        Arguments.of("X", MeasureConditionCode.X),
        Arguments.of("Y", MeasureConditionCode.Y),
        Arguments.of("Z", MeasureConditionCode.Z),
        Arguments.of("YA", MeasureConditionCode.YA),
        Arguments.of("YB", MeasureConditionCode.YB),
        Arguments.of("YC", MeasureConditionCode.YC),
        Arguments.of("YD", MeasureConditionCode.YD));
  }

  @ParameterizedTest
  @MethodSource("measureConditionCodesArgument")
  void shouldGetAppropriateMeasureConditionCode(
      String measureConditionCode, MeasureConditionCode expectedMeasureConditionCode) {
    // when
    MeasureConditionCode actualMeasureConditionCode =
        MeasureConditionCode.from(measureConditionCode);

    // then
    assertThat(actualMeasureConditionCode).isEqualTo(expectedMeasureConditionCode);
  }

  @Test
  void shouldGetUNKNOWNMeasureConditionCodeIfNotRecognisedByGS() {
    // given
    String measureConditionCode = "AA";

    // when
    MeasureConditionCode actualMeasureConditionCode =
        MeasureConditionCode.from(measureConditionCode);

    // then
    assertThat(actualMeasureConditionCode).isEqualTo(MeasureConditionCode.UNKNOWN);
  }
}
