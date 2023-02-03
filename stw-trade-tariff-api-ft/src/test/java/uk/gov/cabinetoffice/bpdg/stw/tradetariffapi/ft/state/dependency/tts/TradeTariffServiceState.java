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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.dependency.tts;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.MockState;

@Component
@Data
@Scope("cucumber-glue")
public class TradeTariffServiceState implements MockState {

  @Autowired private WireMock wireMock;

  private StubMapping primedResponse;

  @Override
  public void prime() {
    if (primedResponse != null) {
      wireMock.register(primedResponse);
    }
  }

  @Override
  public void reset() {
    wireMock.resetRequests();
    wireMock.resetMappings();
  }
}
