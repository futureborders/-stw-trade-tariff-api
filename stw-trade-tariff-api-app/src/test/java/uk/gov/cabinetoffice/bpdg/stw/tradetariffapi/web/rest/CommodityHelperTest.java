/*
 * Copyright 2021 Crown Copyright (Single Trade Window)
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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

@SuppressWarnings("lgtm[java/unused-reference-type]")
class CommodityHelperTest {

  @Test
  void shouldAppend00ForCommodityWhichIsOf8Digits() {
    // given
    String eightDigitCommodityCode = "12345678";

    // when
    String tidiedCommodityCode = CommodityHelper.tidyCommodityCode(eightDigitCommodityCode);

    // then
    assertThat(tidiedCommodityCode).isEqualTo(eightDigitCommodityCode + "00");
  }

  @Test
  void shouldReturnCommodityAsIsIfItIsNotOf8Digits() {
    assertThat(CommodityHelper.tidyCommodityCode("1234567")).isEqualTo("1234567");
    assertThat(CommodityHelper.tidyCommodityCode("123456789")).isEqualTo("123456789");
  }
}
