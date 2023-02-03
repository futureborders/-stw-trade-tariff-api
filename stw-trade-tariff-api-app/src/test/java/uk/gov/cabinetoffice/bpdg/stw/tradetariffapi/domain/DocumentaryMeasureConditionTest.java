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
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionType.CERTIFICATE;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureConditionType.EXCEPTION;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentaryMeasureConditionTest {

  @Test
  void shouldIdentityACertificate() {
    DocumentaryMeasureCondition documentaryMeasureCondition =
        DocumentaryMeasureCondition.builder().documentCode("C123").build();

    assertThat(documentaryMeasureCondition.getMeasureConditionType()).isEqualTo(CERTIFICATE);
  }

  @Test
  void shouldIdentityAsAnExceptionIfDocumentCodeStartsWithY() {
    DocumentaryMeasureCondition documentaryMeasureCondition =
        DocumentaryMeasureCondition.builder().documentCode("Y123").build();

    assertThat(documentaryMeasureCondition.getMeasureConditionType()).isEqualTo(EXCEPTION);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "C084", "9009", "9010", "9011", "9015", "9016", "9020", "9021", "9033", "9034", "9035",
        "9036", "999L"
      })
  void
      shouldIdentityAsAnExceptionForDocumentCodeWhichDoesNotStartWithYAsTheyAreIncorrectlySetupAtSource(
          String documentCode) {
    DocumentaryMeasureCondition documentaryMeasureCondition =
        DocumentaryMeasureCondition.builder().documentCode(documentCode).build();

    assertThat(documentaryMeasureCondition.getMeasureConditionType()).isEqualTo(EXCEPTION);
  }

  @Test
  void shouldReturnDocumentCodeAsMeasureConditionKey() {
    String documentCode = "123";
    DocumentaryMeasureCondition documentaryMeasureCondition =
        DocumentaryMeasureCondition.builder().documentCode(documentCode).build();

    assertThat(documentaryMeasureCondition.getMeasureConditionKey()).isEqualTo(documentCode);
  }

  @Test
  void shouldGetDocumentaryMeasureConditionFields() {
    String id = "122";
    String action = "Entry into free circulation allowed";
    String condition =
        "E: The quantity or the price per unit declared, as appropriate, is equal or less than the specified maximum, or presentation of the required document";
    MeasureConditionCode conditionCode = MeasureConditionCode.M;
    String documentCode = "C652";
    String requirement =
        "Other certificates: Accompanying documents for the carriage of wine products";
    String certificateDescription = "Accompanying documents for the carriage of wine products";

    DocumentaryMeasureCondition documentaryMeasureCondition =
        DocumentaryMeasureCondition.builder()
            .id(id)
            .action(action)
            .condition(condition)
            .conditionCode(conditionCode)
            .documentCode(documentCode)
            .requirement(requirement)
            .description(certificateDescription)
            .build();

    assertThat(documentaryMeasureCondition.getId()).isEqualTo(id);
    assertThat(documentaryMeasureCondition.getAction()).isEqualTo(action);
    assertThat(documentaryMeasureCondition.getCondition()).isEqualTo(condition);
    assertThat(documentaryMeasureCondition.getConditionCode()).isEqualTo(conditionCode);
    assertThat(documentaryMeasureCondition.getDocumentCode()).isEqualTo(documentCode);
    assertThat(documentaryMeasureCondition.getRequirement()).isEqualTo(requirement);
    assertThat(documentaryMeasureCondition.getDescription()).isEqualTo(certificateDescription);
  }
}
