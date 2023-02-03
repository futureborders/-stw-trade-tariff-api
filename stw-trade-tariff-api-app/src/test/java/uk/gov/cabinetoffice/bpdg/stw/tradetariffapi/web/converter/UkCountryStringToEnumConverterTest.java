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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.EnumNotFoundException;

class UkCountryStringToEnumConverterTest {

  private final UkCountryStringToEnumConverter ukCountryStringToEnumConverter =
      new UkCountryStringToEnumConverter();

  private static Stream<Arguments> countryCodesArgument() {
    return Stream.of(
        Arguments.of("gb", UkCountry.GB),
        Arguments.of("GB", UkCountry.GB),
        Arguments.of("xi", UkCountry.XI),
        Arguments.of("XI", UkCountry.XI));
  }

  @ParameterizedTest
  @MethodSource("countryCodesArgument")
  void shouldReturnRespectiveCountryCode(String inputCountryCode, UkCountry expectedCountry) {
    // when
    UkCountry ukCountry = ukCountryStringToEnumConverter.convert(inputCountryCode);

    // then
    assertThat(ukCountry).isEqualTo(expectedCountry);
  }

  @Test
  void shouldThrowErrorWhenUnknownCountryIsPassed() {
    EnumNotFoundException enumNotFoundException =
        Assertions.assertThrows(
            EnumNotFoundException.class, () -> ukCountryStringToEnumConverter.convert("unknown"));

    assertThat(enumNotFoundException).hasMessage("Invalid destination country 'unknown'");
  }
}
