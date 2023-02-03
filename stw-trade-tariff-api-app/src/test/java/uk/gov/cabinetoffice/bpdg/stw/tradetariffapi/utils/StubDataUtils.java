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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.utils;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale.CY;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale.EN;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType.EXPORT;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType.IMPORT;

import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.jupiter.params.provider.Arguments;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StubDataUtils {
  static Stream<Arguments> tradeTypeAndLocales() {
    return Stream.of(
        arguments(IMPORT, EN), arguments(IMPORT, CY), arguments(EXPORT, EN), arguments(EXPORT, CY));
  }
}
