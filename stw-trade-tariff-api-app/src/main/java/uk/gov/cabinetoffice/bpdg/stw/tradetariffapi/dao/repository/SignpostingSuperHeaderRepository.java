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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingSuperHeader;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UserType;

@Repository
interface SignpostingSuperHeaderRepository
    extends ReactiveCrudRepository<SignpostingSuperHeader, Integer> {

  @Query(
      "SELECT super_header.id, "
          + "super_header_description.super_header_description as description, "
          + "super_header_description.super_header_explanatory_text as explanatory_text, "
          + "super_header.order_index, "
          + "true as header, "
          + "headers.id as header_id, "
          + "headers.related_entity_type as header_related_entity_type, "
          + "headers_description.header_description as header_description, "
          + "headers_description.header_explanatory_text as header_explanatory_text, "
          + "headers_description.header_link_text as header_link_text, "
          + "headers_description.external_link as header_external_link, "
          + "headers.order_index header_order_index "
          + "FROM signposting_super_headers super_header "
          + "INNER JOIN signposting_super_header_descriptions super_header_description ON super_header.id = super_header_description.super_header_id "
          + "INNER JOIN signposting_step_headers headers on super_header.id = headers.super_header_id "
          + "INNER JOIN signposting_step_header_descriptions headers_description ON headers.id = headers_description.header_id "
          + "WHERE super_header_description.locale = CAST(:locale as locale) "
          + "AND super_header_description.user_type = CAST(:userType as user_type) "
          + "AND headers_description.user_type = CAST(:userType as user_type) "
          + "AND headers_description.locale = CAST(:locale as locale)")
  Flux<SignpostingSuperHeader> findAllByUserTypeAndLocale(UserType userType, Locale locale);
}
