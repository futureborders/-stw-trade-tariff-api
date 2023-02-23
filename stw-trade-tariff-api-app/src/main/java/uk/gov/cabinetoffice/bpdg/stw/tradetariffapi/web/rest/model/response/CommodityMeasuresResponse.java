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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.response;

import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.CommodityHierarchyItem;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Prohibition;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.SignpostingContent;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TaxAndDuty;

@Builder
@Value
public class CommodityMeasuresResponse {

  String commodityCode;
  String commodityDescription;
  @Singular
  List<MeasurePayload> measures;
  List<SignpostingContent> signpostingContents;
  @Singular
  List<Prohibition> prohibitions;
  List<CommodityHierarchyItem> commodityHierarchy;
  TaxAndDuty taxAndDuty;
}
