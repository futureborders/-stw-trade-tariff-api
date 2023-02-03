Feature: Tax and duty
  Scenario: tariffs and taxes with additional code
    When I call the duties API for commodity code 2207100090 with trade type IMPORT and origin country code MA and destination country code GB
    Then I should get a 200 response
    And the following taxes should be returned
    | text            | value                   | additionalCode  | additionalCodeDescription                   |
    | Excises         | 28.74 GBP / l alc. 100% | X451            | 451 - Spirits other than UK-produced whisky |
    | Value added tax | 20.00 %                 |                 |                                             |
    And the following tariffs should be returned
    | text                | value          | additionalCode  | additionalCodeDescription                           | geographicalAreaId  | geographicalAreaDescription |
    | Third country duty  | 0.00 %         | 2600            | The product I am importing is COVID-19 critical     | 1011                | ERGA OMNES                  |
    | Third country duty  | 16.00 GBP / hl | 2601            | The product I am importing is not COVID-19 critical | 1011                | ERGA OMNES                  |
    | Tariff preference   | 0.00 %         |                 |                                                     | MA                  | Morocco                     |

  Scenario: tariffs and taxes with quota number
    When I call the duties API for commodity code 0702000007 with trade type IMPORT and origin country code MA and destination country code GB
    Then I should get a 200 response
    And the following taxes should be returned
    | text            | value  | additionalCode | additionalCodeDescription |
    | Value added tax | 0.00 % | VATZ           | VAT zero rate             |
    And the following tariffs should be returned
    | text                          | value   | quotaNumber   | geographicalAreaId  | geographicalAreaDescription |
    | Non preferential tariff quota | 12.00 % | 050094        | 1011                | ERGA OMNES                  |
    | Preferential tariff quota     | 0.00 %  | 051104        | MA                  | Morocco                     |
    | Tariff preference             | 5.70 %  |               | MA                  | Morocco                     |
    | Third country duty            | 14.00 % |               | 1011                | ERGA OMNES                  |

  Scenario: tariffs and taxes with quota number for welsh
    When I call the duties API for commodity code 0702000007 with trade type IMPORT origin country code MA destination country code GB and locale CY
    Then I should get a 200 response
    And the following taxes should be returned
      | text                       | value  | additionalCode | additionalCodeDescription |
      | Value added tax cy content | 0.00 % | VATZ           | VAT zero rate             |
    And the following tariffs should be returned
      | text                          | value   | quotaNumber   | geographicalAreaId  | geographicalAreaDescription |
      | Non preferential tariff quota | 12.00 % | 050094        | 1011                | ERGA OMNES                  |
      | Preferential tariff quota     | 0.00 %  | 051104        | MA                  | Morocco                     |
      | Tariff preference             | 5.70 %  |               | MA                  | Morocco                     |
      | Third country duty            | 14.00 % |               | 1011                | ERGA OMNES                  |
