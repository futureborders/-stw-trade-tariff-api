/*
 * Copyright 2021 Crown Copyright (Single Trade Window)
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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.request;

import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.config.AppProperties.CONTEXT_ROOT;

import java.util.Collections;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class GetMeasuresRequestEntity extends RequestEntity {
  String commodityCode;
  String tradeType;
  String originCountry;
  String destinationCountry;
  String additionalCode;
  String importDate;

  @Override
  public Map<String, String> getHeaders() {
    return Collections.emptyMap();
  }

  @Override
  public String path() {
    return buildUri(
        commodityCode,
        tradeType,
        originCountry,
        destinationCountry,
        additionalCode,
        importDate);
  }

  private String buildUri(
      String commodityCode,
      String tradeType,
      String originCountry,
      String destinationCountry,
      String additionalCode,String importDate) {
    StringBuilder stringBuilder = new StringBuilder(CONTEXT_ROOT + "/v1/commodities");
    if (StringUtils.isNotBlank(commodityCode)) {
      stringBuilder.append("/").append(commodityCode);
    }

    stringBuilder.append("/restrictive-measures");

    if (StringUtils.isNotBlank(tradeType) || StringUtils.isNotBlank(originCountry)) {
      stringBuilder.append("?");
      if (StringUtils.isNotBlank(tradeType)) {
        stringBuilder.append("tradeType=").append(tradeType);
        if (StringUtils.isNotBlank(originCountry)) {
          stringBuilder.append("&originCountry=").append(originCountry);
        }
      } else if (StringUtils.isNotBlank(originCountry)) {
        stringBuilder.append("originCountry=").append(originCountry);
      }
      if (StringUtils.isNotBlank(destinationCountry)) {
        stringBuilder.append("&destinationCountry=").append(destinationCountry);
      }
      if (StringUtils.isNotBlank(additionalCode)) {
        stringBuilder.append("&additionalCode=").append(additionalCode);
      }
      if (StringUtils.isNotBlank(importDate)) {
        stringBuilder.append("&importDate=").append(importDate);
      }
    }
    return stringBuilder.toString();
  }
}
