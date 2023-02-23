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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.dependency;

import java.util.Set;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.MockState;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.dependency.db.DatabaseState;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.dependency.tts.TradeTariffServiceState;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.util.ReflectionUtils;

@Component
@Scope("cucumber-glue")
@Data
public class DependencyState {

  @Autowired DatabaseState databaseState;
  @Autowired TradeTariffServiceState tradeTariffServiceState;

  public void reset() {
    mockStates().forEach(MockState::reset);
  }

  public void prime() {
    mockStates().forEach(MockState::prime);
  }

  private Set<MockState> mockStates() {
    return ReflectionUtils.getFieldsOfType(
        this, uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.MockState.class);
  }
}
