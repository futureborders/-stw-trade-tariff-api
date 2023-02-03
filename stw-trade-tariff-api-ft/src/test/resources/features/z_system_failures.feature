Feature: The API should handle downstream system failures gracefully

  Scenario: When V2 API returns 500
    Given The DIT trade tariff API is returning 500 errors
    When I call the measures API for commodity code 2204299710 with trade type IMPORT and origin country code AU and destination country code GB
    Then I should get a 500 response

 Scenario: When V2 API is slow then a timeout exception should occur
   Given The DIT trade tariff API is slow to respond
   When I call the measures API for commodity code 2204299710 with trade type IMPORT and origin country code AU and destination country code GB
   Then I should get a 500 response
