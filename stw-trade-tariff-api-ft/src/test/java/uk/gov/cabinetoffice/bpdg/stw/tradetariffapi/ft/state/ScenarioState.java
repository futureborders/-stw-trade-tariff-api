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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state;

import javax.inject.Named;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.config.AppUnderTest;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.dependency.DependencyState;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.request.RequestEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.response.ResponseEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.response.ResponseState;

@Named
@Scope("cucumber-glue")
@Data
public class ScenarioState {

  @Autowired private AppUnderTest app;

  @Autowired private DependencyState dependencyState;

  private RequestEntity requestEntity;

  private ResponseState responseState;

  public void sendAppRequest() {
    dependencyState.reset();
    dependencyState.prime();
    ResponseEntity responseEntity = app.send(requestEntity);
    this.responseState = ResponseState.fromResponseEntity(responseEntity);
  }

  public void appHealth() {
    ResponseEntity responseEntity = app.health();
    this.responseState = ResponseState.fromResponseEntity(responseEntity);
  }
}
