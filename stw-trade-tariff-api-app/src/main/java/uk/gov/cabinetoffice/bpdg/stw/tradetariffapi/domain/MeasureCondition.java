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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Value;
import org.springframework.util.StringUtils;

@Builder
@Value
public class MeasureCondition {

  private static final Set<String> EXCEPTION_DOCUMENT_CODES = Set.of("C084", "9009", "9010", "9011", "9015", "9016", "9020", "9021", "999L");

  String id;
  MeasureConditionCode conditionCode;
  String condition;
  String documentCode;
  String requirement;
  String action;
  String dutyExpression;

  public MeasureConditionType getMeasureConditionType() {
    if (StringUtils.hasLength(documentCode)
      && !documentCode.startsWith("Y")
      && !EXCEPTION_DOCUMENT_CODES.contains(documentCode)) {
      return MeasureConditionType.CERTIFICATE;
    }
    if (StringUtils.hasLength(documentCode)
      && (documentCode.startsWith("Y") || EXCEPTION_DOCUMENT_CODES.contains(documentCode))) {
      return MeasureConditionType.EXCEPTION;
    }
    if (!StringUtils.hasLength(documentCode) && StringUtils.hasLength(requirement)) {
      return MeasureConditionType.THRESHOLD;
    }
    return MeasureConditionType.NEGATIVE;
  }

  @JsonIgnore
  public String getMeasureConditionKey() {
    return StringUtils.hasLength(this.getDocumentCode())
      ? this.getDocumentCode()
      : this.getRequirement();
  }
}
