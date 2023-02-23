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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "signposting_steps")
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
public class SignpostingStep {
  @Id private Integer Id;

  @Column(name = "header_id")
  private Integer headerId;

  @Column(name = "step_description")
  private String description;

  @Column(name = "step_howto_description")
  private String howToDescription;

  @Column(name = "step_url")
  private String url;

  @Column(name = "non_declaring_trader_content")
  private String nonDeclaringTraderContent;

  @Column(name = "declaring_trader_content")
  private String declaringTraderContent;

  @Column(name = "agent_content")
  private String agentContent;

  @Column(name = "published")
  private boolean published;

  @Column(name = "destination_country_restrictions")
  @Type(type = "string-array")
  @Builder.Default
  private String[] destinationCountryRestrictions = {"GB", "XI"};

  @Column(name = "origin_country_restrictions")
  @Type(type = "string-array")
  @Builder.Default
  private String[] originCountryRestrictions = {"ALL"};
}
