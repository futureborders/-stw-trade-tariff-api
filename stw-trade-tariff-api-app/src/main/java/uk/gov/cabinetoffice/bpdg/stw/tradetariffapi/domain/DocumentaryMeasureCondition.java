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

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DocumentaryMeasureCondition extends MeasureCondition {

  private static final Set<String> EXCEPTION_DOCUMENT_CODES =
      Set.of(
          "C084", "9009", "9010", "9011", "9015", "9016", "9020", "9021", "9033", "9034", "9035",
          "9036", "999L");

  String documentCode;
  String description;

  @Override
  public MeasureConditionType getMeasureConditionType() {
    if (documentCode.startsWith("Y") || EXCEPTION_DOCUMENT_CODES.contains(documentCode)) {
      return MeasureConditionType.EXCEPTION;
    }
    return MeasureConditionType.CERTIFICATE;
  }

  @Override
  public String getMeasureConditionKey() {
    return documentCode;
  }
}
