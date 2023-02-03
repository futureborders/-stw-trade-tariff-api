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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.response;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseState {
  private Integer status;
  private String payload;
  private final Map<String, String> headers;

  public ResponseState(Integer status, String payload, Map<String, String> headers) {
    this.status = status;
    this.payload = payload;
    this.headers = headers;
  }

  public static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.response.ResponseState fromResponseEntity(ResponseEntity responseEntity) {
    return builder()
        .status(responseEntity.getStatus())
        .headers(responseEntity.getHeaders())
        .payload(responseEntity.getPayload())
        .build();
  }
}
