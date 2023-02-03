Feature: The Additional codes API should return details for any additional codes that are found within the applicable commodity measures

  Scenario: Should return additional codes
    When I call the additional codes API for commodity code 4103900000 with trade type IMPORT and origin country code CN and destination country code GB
    Then I should get a 200 response
    And the response should contain the following additional codes
      | code | description        |
      | 4200 | Procyon lotor      |
      | 4201 | Canis lupus        |
      | 4202 | Martes zibellina   |
      | 4203 | Mustela erminea    |
      | 4204 | Ondatra zibethicus |
      | 4205 | Castor canadensis  |
      | 4206 | Felis rufus        |
      | 4207 | Taxidea taxus      |
      | 4208 | Canis latrans      |
      | 4209 | Lutra canadensis   |
      | 4210 | Lynx canadensis    |
      | 4211 | Martes americana   |
      | 4212 | Martes pennanti    |
      | 4999 | Other              |

  Scenario: Should return additional codes
    When I call the additional codes API for commodity code 3824994500 with trade type EXPORT and origin country code XI and destination country code CN
    Then I should get a 200 response
    And the response should contain the following additional codes
      | code | description        |
      | 3200 | Mixtures of scheduled substances listed in the Annex to Regulation (EC) No 111/2005 that can be used for the illicit manufacture of narcotic drugs or psychotropic substances      |
      | 3249 | Other              |

  Scenario: Should return error when the commodity is not found in DIT trade tariff API
    Given The DIT trade tariff API is returning not found for commodity 1000000000
    When I call the additional codes API for commodity code 1000000000 with trade type IMPORT and origin country code CN and destination country code GB
    Then I should get a 404 response with message Resource 'Commodity' not found with id '1000000000'

  Scenario Outline: Should return error when mandatory query params are missing
    When I call the additional codes API for commodity code <commodity_code> with trade type <trade_type> and origin country code <country_code> and destination country code <destination_country>
    Then I should get a <http_status> response
    Then I should get the following validation errors
      | fieldName     | message         |
      | <error_field> | <error_message> |
    Examples:
      | commodity_code | trade_type | country_code | destination_country | http_status | error_field        | error_message                     |
      | 1006107900     |            | CN           | GB                  | 400         | tradeType          | must not be null                  |
      | 1006107900     | DUNNO      | CN           | GB                  | 400         | tradeType          | TradeType 'DUNNO' does not exist  |
      | 1006107900     | IMPORT     |              | GB                  | 400         | originCountry      | must not be null                  |
      | 1006107900     | IMPORT     | X            | GB                  | 400         | originCountry      | must match "^\w{2}$"              |
      | 1006107900     | IMPORT     | XYY          | GB                  | 400         | originCountry      | must match "^\w{2}$"              |
      | 1006107900     | IMPORT     | CHINA        | GB                  | 400         | originCountry      | must match "^\w{2}$"              |
      | Grapefruit     | IMPORT     | CN           | GB                  | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"      |
      | 100610790000   | IMPORT     | CN           | GB                  | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"      |
      | 1006107900     | IMPORT     | CN           |                     | 400         | destinationCountry | must not be null                  |
      | 1006107900     | IMPORT     | CN           | ABC                 | 400         | destinationCountry | must match "^\w{2}$"              |
      | 1006107900     | IMPORT     | CN           | HK                  | 400         | destinationCountry | Destination country HK is not a valid UK country |
      | 1006107900     | EXPORT     | CN           | HK                  | 400         | originCountry      | Origin country CN is not a valid UK country |
