Feature: If an additional code is selected, then the system should return more succinct measures results
  - Conditions for the measure corresponding to selected non-residual additional code should be returned
  - Omit all measures with additional codes results if a residual additional code is selected
  - Return all measures if the selected additional code does not match any measures

  Scenario: When a Non-residual code is selected, then out of all commodity measures with additional codes,
  only the measure matching the selected code should be returned
    When I call the measures API for commodity code 4103900000 with additional code 4206 and trade type IMPORT and origin country code CN and destination country code GB
    Then the response includes the following measures
      | measureId | measureDescriptionOverlay                  | numberOfInstances |
      | 410       | Veterinary control                         | 1                 |
      | 350       | Animal Health Certificate                  | 1                 |
      | 475       | Restriction on entry into free circulation | 1                 |
      | 710       | Import control - CITES                     | 1                 |
      | 745       | Import control on cat and dog fur          | 1                 |

  Scenario: When a residual code is selected, then all commodity measures with additional codes are excluded from the response
    When I call the measures API for commodity code 4103900000 with additional code 4999 and trade type IMPORT and origin country code CN and destination country code GB
    Then the response includes the following measures
      | measureId | measureDescriptionOverlay         | numberOfInstances |
      | 410       | Veterinary control                | 1                 |
      | 350       | Animal Health Certificate         | 1                 |
      | 710       | Import control - CITES            | 1                 |
      | 745       | Import control on cat and dog fur | 1                 |

  Scenario: When the selected code does not match any additional codes in the response, then all commodity measures should be returned
    When I call the measures API for commodity code 4103900000 with additional code 0000 and trade type IMPORT and origin country code CN and destination country code GB
    Then the response includes the following measures
      | measureId | measureDescriptionOverlay                  | numberOfInstances |
      | 410       | Veterinary control                         | 1                 |
      | 350       | Animal Health Certificate                  | 1                 |
      | 710       | Import control - CITES                     | 1                 |
      | 745       | Import control on cat and dog fur          | 1                 |
      | 475       | Restriction on entry into free circulation | 13                |

