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

public enum MeasureConditionCode {
  A(null),
  B(null),
  C(null),
  D(null),
  E(ThresholdType.MAX),
  F(ThresholdType.MIN),
  G(ThresholdType.MIN),
  H(null),
  I(ThresholdType.MAX),
  J(null),
  K(null),
  L(ThresholdType.MIN),
  M(ThresholdType.MIN),
  N(ThresholdType.MIN),
  O(null),
  P(null),
  Q(null),
  R(ThresholdType.MIN),
  S(null),
  U(ThresholdType.MIN),
  V(ThresholdType.MIN),
  W(null),
  X(null),
  Y(null),
  Z(null),
  YA(null),
  YB(null),
  YC(null),
  YD(null),
  UNKNOWN(null);

  private final ThresholdType thresholdType;

  MeasureConditionCode(ThresholdType thresholdType) {
    this.thresholdType = thresholdType;
  }

  public ThresholdType getThresholdType() {
    return thresholdType;
  }

  public static MeasureConditionCode from(String measureConditionCode){
    for(MeasureConditionCode conditionCode: MeasureConditionCode.values()){
      if(conditionCode.name().equals(measureConditionCode)){
        return conditionCode;
      }
    }
    return UNKNOWN;
  }
}
