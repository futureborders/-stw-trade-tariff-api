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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class TariffTest {

  @Test
  void shouldGetAdditionCode(){
    Tariff tariff = Tariff.builder().build();
    assertThat(tariff.getAdditionalCode()).isEmpty();

    AdditionalCode additionalCode = AdditionalCode.builder().build();
    tariff = Tariff.builder().additionalCode(additionalCode).build();
    assertThat(tariff.getAdditionalCode()).isNotEmpty();
    assertThat(tariff.getAdditionalCode().get()).isEqualTo(additionalCode);
  }

  @Test
  void shouldGetQuota(){
    Tariff tariff = Tariff.builder().build();
    assertThat(tariff.getQuota()).isEmpty();

    Quota quota = Quota.builder().build();
    tariff = Tariff.builder().quota(quota).build();
    assertThat(tariff.getQuota()).isNotEmpty();
    assertThat(tariff.getQuota().get()).isEqualTo(quota);
  }

}
