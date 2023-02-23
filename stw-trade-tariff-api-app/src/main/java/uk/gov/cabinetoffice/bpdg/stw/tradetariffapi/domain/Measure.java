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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Builder
@Value
public class Measure {

  String id;
  boolean taxMeasure;
  List<TradeType> applicableTradeTypes;
  MeasureType measureType;
  GeographicalArea geographicalArea;
  AdditionalCode additionalCode;
  String dutyValue;
  String quotaNumber;
  @Singular
  List<MeasureCondition> measureConditions;
  String legalActId;
  @Singular
  Set<String> excludedCountries;

  public Optional<AdditionalCode> getAdditionalCode() {
    return Optional.ofNullable(additionalCode);
  }

  public Optional<String> getDutyValue() {
    return Optional.ofNullable(dutyValue);
  }

  public Optional<String> getQuotaNumber() {
    return Optional.ofNullable(quotaNumber);
  }
}
