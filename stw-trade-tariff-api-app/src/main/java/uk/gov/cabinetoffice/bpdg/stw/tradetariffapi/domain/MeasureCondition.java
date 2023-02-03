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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode
@ToString
public abstract class MeasureCondition {

  @Getter(AccessLevel.PUBLIC)
  String id;

  MeasureConditionCode conditionCode;

  @Getter(AccessLevel.PUBLIC)
  String condition;

  @Getter(AccessLevel.PUBLIC)
  String requirement;

  @Getter(AccessLevel.PUBLIC)
  String action;

  public abstract MeasureConditionType getMeasureConditionType();

  @JsonIgnore
  public abstract String getMeasureConditionKey();

  public MeasureConditionCode getConditionCode() {
    return conditionCode;
  }
}
