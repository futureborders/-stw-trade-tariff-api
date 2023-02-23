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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UkCountry;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("signposting_steps")
public class SignpostingStep {

  public static final String ALL_COUNTRY_RESTRICTION = "ALL";
  public static final String EU_COUNTRY_RESTRICTION = "EU";

  @Id
  private Integer id;
  private String stepDescription;
  private String stepHowtoDescription;
  private String stepUrl;
  private String nonDeclaringTraderContent;
  private String declaringTraderContent;
  private String agentContent;
  private SignpostingSuperHeader superHeader;
  private SignpostingStepHeader header;
  private boolean published;

  @Builder.Default
  private Set<UkCountry> destinationCountryRestrictions = Set.of(UkCountry.GB, UkCountry.XI);

  @Builder.Default
  private Set<String> originCountryRestrictions = Set.of(ALL_COUNTRY_RESTRICTION);
}
