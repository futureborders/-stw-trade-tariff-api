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

import lombok.AllArgsConstructor;
import lombok.Value;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.Chapter;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.Commodity;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.Section;
import uk.gov.cabinetoffice.bpdg.stw.external.hmrc.tradetariff.model.commodity.relationships.CommodityHeading;

@Value
@AllArgsConstructor
public class CommodityHierarchyItem {

  String id;
  String description;
  CommodityHierarchyType type;

  public CommodityHierarchyItem(Section section) {
    this.id = section.getId();
    this.description = section.getDescription();
    this.type = CommodityHierarchyType.SECTION;
  }

  public CommodityHierarchyItem(Chapter chapter) {
    this.id = chapter.getGoodsNomenclatureItemId();
    this.description = chapter.getDescription();
    this.type = CommodityHierarchyType.CHAPTER;
  }

  public CommodityHierarchyItem(CommodityHeading commodityHeading) {
    this.id = commodityHeading.getGoodsNomenclatureItemId();
    this.description = commodityHeading.getDescription();
    this.type = CommodityHierarchyType.HEADING;
  }

  public CommodityHierarchyItem(Commodity commodity) {
    this.id = commodity.getGoodsNomenclatureItemId();
    this.description = commodity.getDescription();
    this.type = CommodityHierarchyType.COMMODITY;
  }
}
