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

import org.springframework.core.convert.converter.Converter;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.EnumNotFoundException;

public class LocaleStringToEnumConverter implements Converter<String, Locale> {

  @Override
  public Locale convert(String locale) {
    try {
      return Locale.valueOf(locale.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new EnumNotFoundException(Locale.class, locale);
    }
  }
}
