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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.converter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.EnumNotFoundException;

class LocaleStringToEnumConverterTest {
  LocaleStringToEnumConverter localeStringToEnumConverter = new LocaleStringToEnumConverter();

  @Test
  void shouldConvertToEnLocale() {
    Locale result = localeStringToEnumConverter.convert("EN");
    assertThat(Locale.EN).isEqualTo(result);
    result = localeStringToEnumConverter.convert("en");
    assertThat(Locale.EN).isEqualTo(result);
  }

  @Test
  void shouldConvertToCyLocale() {
    Locale result = localeStringToEnumConverter.convert("CY");
    assertThat(Locale.CY).isEqualTo(result);
    result = localeStringToEnumConverter.convert("cy");
    assertThat(Locale.CY).isEqualTo(result);
  }

  @Test
  void shouldThrowEnumNotFoundException() {
    assertThatThrownBy(() -> localeStringToEnumConverter.convert("abc"))
        .isInstanceOf(EnumNotFoundException.class);
  }
}
