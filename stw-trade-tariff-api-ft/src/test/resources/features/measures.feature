Feature: Measures API should return the correct measures based on given inputs

  Scenario: Simple measure containing one certificate and an exception
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay              | tradeType | locale |
      | 1  | 750           | Import control of organic products | IMPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                                             | tradeType | locale |
      | 1  | C644         | You need a certificate of inspection for organic products                      | IMPORT    | EN     |
      | 2  | Y929         | Your goods are not concerned by Regulation (EC) No 834/2007 (organic products) | IMPORT    | EN     |
    When I call the measures API for commodity code 2204299710 with trade type IMPORT and origin country code AU and destination country code GB
    Then the response includes the measures option data below under measure Import control of organic products with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay                                                                                | measureOptionCertificateCode |
      | CERTIFICATE       | You need a certificate of inspection for organic products                                                      | C644                         |
      | EXCEPTION         | If your goods are not concerned by Regulation (EC) No 834/2007 (organic products), then your goods are exempt. | Y929                         |

  Scenario: Simple measure containing one certificate and an exception for exports
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay   | tradeType | locale |
      | 1  | 478           | Export authorization | EXPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                                 | tradeType | locale |
      | 1  | X002         | Export licence: Dual use export authorisation more desc            | EXPORT    | EN     |
      | 2  | Y999         | Goods for which an export licence is not required. Please use this | EXPORT    | EN     |
    When I call the measures API for commodity code 2804501000 with trade type EXPORT and origin country code GB and destination country code AU
    Then the response includes the measures option data below under measure Export authorization with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay                                    | measureOptionCertificateCode |
      | CERTIFICATE       | Export licence: Dual use export authorisation more desc            | X002                         |
      | EXCEPTION         | Goods for which an export licence is not required. Please use this | Y999                         |

  Scenario: Simple measure containing one certificate and an exception for welsh
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay                 | tradeType | locale |
      | 1  | 750           | Import control of organic products | IMPORT    | CY     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                                                        | tradeType | locale |
      | 1  | C644         | You need a certificate of inspection for organic products                                 | IMPORT    | EN     |
      | 2  | Y929         | Your goods are not concerned by Regulation (EC) No 834/2007 (organic products) cy content | IMPORT    | CY     |
    When I call the measures API for commodity code 2204299710 with trade type IMPORT and origin country AU and destination country GB and locale CY
    Then the response includes the measures option data below under measure Import control of organic products cy content with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay                                                                                           | measureOptionCertificateCode |
      | CERTIFICATE       | You need a certificate of inspection for organic products                                                                 | C644                         |
      | EXCEPTION         | If your goods are not concerned by Regulation (EC) No 834/2007 (organic products) cy content, then your goods are exempt. | Y929                         |

  Scenario: Simple measure containing certificates and a price based threshold for exports
    When I call the measures API for commodity code 0101210000 with trade type EXPORT and origin country code GB and destination country code BY
    Then the response includes the measures option data below under measure Restriction on export with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionSubtype | measureOptionUnit | measureOptionDescriptionOverlay                                                                                                                                                                                                                 | measureOptionCertificateCode |
      | CERTIFICATE       |                      |                   | All other import documents (including Department of Health) licences, permits or certificates other than those specifically identified by other document codes.                                                                                 | 9005                         |
      | CERTIFICATE       |                      |                   | This exemption applies to sanctioned goods are necessary for the official purposes of a diplomatic mission or consular post in Russia or Belarus, or of an international organisation enjoying immunities in accordance with international law. | 9006                         |
      | CERTIFICATE       |                      |                   | Goods exported to Belarus before 05 July 2022.                                                                                                                                                                                                  | 9027                         |
      | EXCEPTION         |                      |                   | Goods subject to sanction with a DIT import or export licence                                                                                                                                                                                   | 9011                         |
      | EXCEPTION         |                      |                   | Goods not subject to sanction                                                                                                                                                                                                                   | Y080                         |
      | THRESHOLD         | PRICE_BASED          |                   | If the value of your shipment is less than £250, then your goods are exempt.                                                                                                                                                                    |                              |

  Scenario: Simple measure containing certificates and a price per unit based threshold for exports
    When I call the measures API for commodity code 0711590019 with trade type EXPORT and origin country code GB and destination country code BY
    Then the response includes the measures option data below under measure Restriction on export with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionSubtype | measureOptionUnit | measureOptionDescriptionOverlay                                                                                                                                                                                                                 | measureOptionCertificateCode |
      | CERTIFICATE       |                      |                   | All other import documents (including Department of Health) licences, permits or certificates other than those specifically identified by other document codes.                                                                                 | 9005                         |
      | CERTIFICATE       |                      |                   | This exemption applies to sanctioned goods are necessary for the official purposes of a diplomatic mission or consular post in Russia or Belarus, or of an international organisation enjoying immunities in accordance with international law. | 9006                         |
      | CERTIFICATE       |                      |                   | Goods exported to Belarus before 05 July 2022.                                                                                                                                                                                                  | 9027                         |
      | EXCEPTION         |                      |                   | Goods subject to sanction with a DIT import or export licence                                                                                                                                                                                   | 9011                         |
      | EXCEPTION         |                      |                   | Goods not subject to sanction                                                                                                                                                                                                                   | Y080                         |
      | THRESHOLD         | PRICE_PER_UNIT_BASED | kilogram          | If the value of your shipment is less than £167 / kilogram, then your goods are exempt.                                                                                                                                                         |                              |

  Scenario: Simple measure containing multiple certificates and a volume based threshold
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay                         | tradeType | locale |
      | 1  | 465           | Restriction on entry into free circulation | IMPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay              | tradeType | locale |
      | 1  | C014         | You need a VI1 document         | IMPORT    | EN     |
      | 2  | C015         | You need a VI2 extract document | IMPORT    | EN     |
    When I call the measures API for commodity code 2204299710 with trade type IMPORT and origin country code CN and destination country code GB
    Then the response includes the measures option data below under measure Restriction on entry into free circulation with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionSubtype | measureOptionUnit | measureOptionDescriptionOverlay                                       | measureOptionCertificateCode |
      | CERTIFICATE       |                      |                   | Check if you need a VI1 document                                      | C014                         |
      | CERTIFICATE       |                      |                   | Check if you need a VI2 extract document                              | C015                         |
      | THRESHOLD         | VOLUME_BASED         |                   | If your shipment is less than 100 litres, then your goods are exempt. |                              |

  Scenario: Simple measure containing one certificate and one weight based threshold
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay                         | tradeType | locale |
      | 1  | 465           | Restriction on entry into free circulation | IMPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                       | tradeType | locale |
      | 1  | C652         | Accompanying documents for the carriage of wine products | IMPORT    | EN     |
    When I call the measures API for commodity code 2204299710 with trade type IMPORT and origin country code CH and destination country code GB
    Then the response includes the measures option data below under measure Restriction on entry into free circulation with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionSubtype | measureOptionUnit | measureOptionDescriptionOverlay                                             | measureOptionCertificateCode |
      | CERTIFICATE       |                      |                   | Accompanying documents for the carriage of wine products                    | C652                         |
      | THRESHOLD         | WEIGHT_BASED         |                   | If your shipment weigh less than 30 kilograms, then your goods are exempt. |                              |

  Scenario: Simple measure containing one certificate and one unit based threshold
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay                         | tradeType | locale |
      | 1  | 410           | Veterinary control                               | IMPORT    | EN     |
      | 2  | 465           | Restriction on entry into free circulation | IMPORT    | EN      |
      | 2  | 750           | Import control of organic products               | IMPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                       | tradeType | locale |
      | 1  | C084         | Your goods are intended for scientific purposes, research or diagnostic samples | IMPORT    | EN     |
      | 2  | Y058         | Your goods are for personal consumption or use           | IMPORT    | EN     |
    When I call the measures API for commodity code 0306150000 with trade type IMPORT and origin country code AF and destination country code GB
    Then the response includes the measures option data below under measure Veterinary control with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionSubtype | measureOptionUnit | measureOptionDescriptionOverlay                                             | measureOptionCertificateCode |
      | CERTIFICATE       |                      |                   | You need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)| N853 |
      | EXCEPTION       |                      |                   | If your goods are for scientific or research usage or for use as diagnostic samples, then your goods are exempt.| C084|
      | THRESHOLD_CERTIFICATE |          |                 | If your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use and the unit is less than 20, then your goods are exempt. |Y058|



  Scenario: Complex measure containing one common certificate, one exception and one exception+threshold
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay                               | tradeType | locale |
      | 1  | 410           | Veterinary control                               | IMPORT    | EN     |
      | 2  | 750           | Import control of organic products               | IMPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                                                                                        | tradeType | locale |
      | 1  | N853         | You need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)  | IMPORT    | EN     |
      | 2  | C084         | Your goods are for scientific or research usage or for use as diagnostic samples.                                         | IMPORT    | EN     |
      | 3  | Y058         | Your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use. | IMPORT    | EN     |
      | 4  | C644         | You need a certificate of inspection for organic products                                                                 | IMPORT    | EN     |
      | 5  | Y929         | not organic products                                                                                        | IMPORT    | EN     |
    When I call the measures API for commodity code 0208907000 with trade type IMPORT and origin country code CN and destination country code GB
    Then the response includes the measures option data below under measure Veterinary control with measure type series B and measure type RESTRICTIVE
      | measureOptionType     | measureOptionDescriptionOverlay                                                                                                                                                           | measureOptionCertificateCode |
      | CERTIFICATE           | You need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)                                                                  | N853                         |
      | EXCEPTION             | If your goods are for scientific or research usage or for use as diagnostic samples, then your goods are exempt.                                                                          | C084                         |
      | THRESHOLD_CERTIFICATE | If your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use and weigh less than 2 kilograms, then your goods are exempt. | Y058                         |

  Scenario: For a complex measure containing a common certificate, common threshold, common exception and disjoint certificates, the disjoint certificates should be combined into a single measure option
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay       | tradeType | locale |
      | 1  | 755           | Import control for waste | IMPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                                                                           | tradeType | locale |
      | 1  | C672         | You need information document for export of non-hazardous waste or imports of non-hazardous waste from EU    | IMPORT    | EN     |
      | 2  | C669         | You need Notification document for import/export of hazardous or mixed notifiable waste.                     | IMPORT    | EN     |
      | 3  | Y923         | Your goods Products not considered as waste according to Regulation (EC) No 1013/2006 as retained in UK law. | IMPORT    | EN     |
      | 4  | C670         | You need a movement document for import/export of hazardous or mixed notifiable waste.                       | IMPORT    | EN     |
    When I call the measures API for commodity code 2529220000 with trade type IMPORT and origin country code CN and destination country code GB
    Then the response includes the measures option data below under measure Import control for waste with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionSubtype | measureOptionUnit | measureOptionDescriptionOverlay                                                                                                                                                    | measureOptionCertificateCode |
      | THRESHOLD         | WEIGHT_BASED         |                   | If your shipment weigh less than 20 kilograms, then your goods are exempt.                                                                                                        |                              |
      | CERTIFICATE       |                      |                   | You need information document for export of non-hazardous waste or imports of non-hazardous waste from EU                                                                          | C672                         |
      | EXCEPTION         |                      |                   | If your goods Products not considered as waste according to Regulation (EC) No 1013/2006 as retained in UK law, then your goods are exempt.                                        | Y923                         |
      | MULTI_CERTIFICATE |                      |                   | You need Notification document for import/export of hazardous or mixed notifiable waste and you need a movement document for import/export of hazardous or mixed notifiable waste. | C669 & C670                  |

  Scenario: For a complex measure containing a common certificate, common threshold, common exception and disjoint certificates, the disjoint certificates should be combined into a single measure option for exports
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay       | tradeType | locale |
      | 1  | 751           | Export control for Waste | EXPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                                                                           | tradeType | locale |
      | 1  | C672         | You need information document for export of non-hazardous waste or imports of non-hazardous waste from EU    | EXPORT    | EN     |
      | 2  | C669         | You need Notification document for import/export of hazardous or mixed notifiable waste.                     | EXPORT    | EN     |
      | 3  | Y923         | Your goods Products not considered as waste according to Regulation (EC) No 1013/2006 as retained in UK law. | EXPORT    | EN     |
      | 4  | C670         | You need a movement document for import/export of hazardous or mixed notifiable waste.                       | EXPORT    | EN     |
    When I call the measures API for commodity code 2501009900 with trade type EXPORT and origin country code GB and destination country code AD
    Then the response includes the measures option data below under measure Export control for Waste with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionSubtype | measureOptionUnit | measureOptionDescriptionOverlay                                                                                                                                                    | measureOptionCertificateCode |
      | THRESHOLD         | WEIGHT_BASED         |                   | If your shipment weigh less than 20 kilograms, then your goods are exempt.                                                                                                        |                              |
      | CERTIFICATE       |                      |                   | You need information document for export of non-hazardous waste or imports of non-hazardous waste from EU                                                                          | C672                         |
      | EXCEPTION         |                      |                   | CDS universal waiver                                                                                                                                                               | 999L                         |
      | EXCEPTION         |                      |                   | If your goods Products not considered as waste according to Regulation (EC) No 1013/2006 as retained in UK law, then your goods are exempt.                                        | Y923                         |
      | MULTI_CERTIFICATE |                      |                   | You need Notification document for import/export of hazardous or mixed notifiable waste and you need a movement document for import/export of hazardous or mixed notifiable waste. | C669 & C670                  |

  Scenario: Measure containing multiple options
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay                                    | tradeType | locale |
      | 1  | 724           | Import control of fluorinated greenhouse gases edited | IMPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay   | tradeType | locale |
      | 1  | C082         | description for C082 | IMPORT    | EN     |
      | 2  | Y951         | description for Y951 | IMPORT    | EN     |
      | 3  | C057         | description for C057 | IMPORT    | EN     |
      | 4  | C079         | description for C079 | IMPORT    | EN     |
      | 5  | Y054         | description for Y054 | IMPORT    | EN     |
      | 6  | Y053         | description for Y053 | IMPORT    | EN     |
      | 7  | Y926         | description for Y926 | IMPORT    | EN     |
    When I call the measures API for commodity code 8415810091 with additional code 4999 and trade type IMPORT and origin country code CN and destination country code GB
    Then the response includes the measures option data below under measure Import control of fluorinated greenhouse gases edited with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay         | measureOptionCertificateCode |
      | EXCEPTION         | CDS universal waiver                    | 999L                         |
      | EXCEPTION         | description for Y926                    | Y926                         |
      And the response includes the measures option data below under measure Import control of fluorinated greenhouse gases edited with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay         | measureOptionCertificateCode |
      | EXCEPTION         | CDS universal waiver                    | 999L                         |
      | EXCEPTION         | description for Y053                    | Y053                         |
      | EXCEPTION         | description for Y054                    | Y054                         |
      And the response includes the measures option data below under measure Import control of fluorinated greenhouse gases edited with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay         | measureOptionCertificateCode |
      | CERTIFICATE       | description for C057                    | C057                         |
      | CERTIFICATE       | description for C079                    | C079                         |
      | CERTIFICATE       | description for C082                    | C082                         |
      | EXCEPTION         | CDS universal waiver                    | 999L                         |
      | EXCEPTION         | description for Y951                    | Y951                         |

  Scenario: Measure containing multiple options with a prohibited additional code
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay                                    | tradeType | locale |
      | 1  | 724           | Import control of fluorinated greenhouse gases edited | IMPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay   | tradeType | locale |
      | 1  | C082         | description for C082 | IMPORT    | EN     |
      | 2  | Y951         | description for Y951 | IMPORT    | EN     |
      | 3  | C057         | description for C057 | IMPORT    | EN     |
      | 4  | C079         | description for C079 | IMPORT    | EN     |
      | 5  | Y054         | description for Y054 | IMPORT    | EN     |
      | 6  | Y053         | description for Y053 | IMPORT    | EN     |
      | 7  | Y926         | description for Y926 | IMPORT    | EN     |
    When I call the measures API for commodity code 8415810091 with additional code 4115 and trade type IMPORT and origin country code CN and destination country code GB
    Then the response includes the measures option data below under measure Import control of fluorinated greenhouse gases edited with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay         | measureOptionCertificateCode |
      | EXCEPTION         | CDS universal waiver                    | 999L                         |
      | EXCEPTION         | description for Y926                    | Y926                         |
      And the response includes the measures option data below under measure Import control of fluorinated greenhouse gases edited with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay         | measureOptionCertificateCode |
      | EXCEPTION         | CDS universal waiver                    | 999L                         |
      | EXCEPTION         | description for Y053                    | Y053                         |
      | EXCEPTION         | description for Y054                    | Y054                         |
      And the response includes the measures option data below under measure Import control of fluorinated greenhouse gases edited with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay         | measureOptionCertificateCode |
      | CERTIFICATE       | description for C057                    | C057                         |
      | CERTIFICATE       | description for C079                    | C079                         |
      | CERTIFICATE       | description for C082                    | C082                         |
      | EXCEPTION         | CDS universal waiver                    | 999L                         |
      | EXCEPTION         | description for Y951                    | Y951                         |

  Scenario: Complex measures should be returned
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay                               | tradeType | locale |
      | 1  | 410           | Veterinary control                               | IMPORT    | EN     |
      | 2  | 750           | Import control of organic products               | IMPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                                                                                        | tradeType | locale |
      | 1  | N853         | You need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)  | IMPORT    | EN     |
      | 2  | C084         | Your goods are for scientific or research usage or for use as diagnostic samples.                                         | IMPORT    | EN     |
      | 3  | Y058         | Your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use. | IMPORT    | EN     |
      | 4  | C644         | You need a certificate of inspection for organic products                                                                 | IMPORT    | EN     |
      | 5  | Y929         | Your goods are not concerned by Regulation (EC) No 834/2007 (organic products)                                            | IMPORT    | EN     |
    When I call the measures API for commodity code 0208907000 with trade type IMPORT and origin country code CN and destination country code GB
    Then the response includes the measures option data below under measure Veterinary control with measure type series B and measure type RESTRICTIVE
      | measureOptionType     | measureOptionDescriptionOverlay                                                                                                                                                           | measureOptionCertificateCode |
      | CERTIFICATE           | You need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)                                                                  | N853                         |
      | EXCEPTION             | If your goods are for scientific or research usage or for use as diagnostic samples, then your goods are exempt.                                                                          | C084                         |
      | THRESHOLD_CERTIFICATE | If your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use and weigh less than 2 kilograms, then your goods are exempt. | Y058                         |
    Then the response includes the measures option data below under measure Import control of organic products with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay                                                                                | measureOptionCertificateCode |
      | CERTIFICATE       | You need a certificate of inspection for organic products                                                      | C644                         |
      | EXCEPTION         | If your goods are not concerned by Regulation (EC) No 834/2007 (organic products), then your goods are exempt. | Y929                         |

  Scenario: Prohibitions should be returned in response for a commodity which has a prohibition along with condition based measures if any - imports
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay                               | tradeType | locale |
      | 1  | 410           | Veterinary control                               | IMPORT    | EN     |
      | 2  | 750           | Import control of organic products               | IMPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                                                                                        | tradeType | locale |
      | 1  | N853         | You need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)  | IMPORT    | EN     |
      | 2  | C084         | Your goods are for scientific or research usage or for use as diagnostic samples.                                         | IMPORT    | EN     |
      | 3  | Y058         | Your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use. | IMPORT    | EN     |
      | 4  | C644         | You need a certificate of inspection for organic products                                                                 | IMPORT    | EN     |
      | 5  | Y929         | Your goods are not concerned by Regulation (EC) No 834/2007 (organic products)                                            | IMPORT    | EN     |
      | 6  | Y999         | Goods for which an export licence is not required. Please use this                                                        | IMPORT    | EN     |
    When I call the measures API for commodity code 0307111010 with trade type IMPORT and origin country code TR and destination country code GB
    Then the response includes the measures option data below under measure Animal Health Certificate with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay                                                                     | measureOptionCertificateCode |
      | CERTIFICATE       | Importation of animal pathogens Licence under the Importation of Animal pathogens Order 1980 (IAPO) | 9120                         |
      | EXCEPTION         | CDS universal waiver                                                                                | 999L                         |
    Then the response includes the measures option data below under measure Import control of organic products with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay                                                                                | measureOptionCertificateCode |
      | CERTIFICATE       | You need a certificate of inspection for organic products                                                      | C644                         |
      | EXCEPTION         | CDS universal waiver                                                                                           | 999L                         |
      | EXCEPTION         | If your goods are not concerned by Regulation (EC) No 834/2007 (organic products), then your goods are exempt. | Y929                         |
    Then the response includes the measures option data below under measure Veterinary control with measure type series B and measure type RESTRICTIVE
      | measureOptionType     | measureOptionDescriptionOverlay                                                                                                                                                            | measureOptionCertificateCode |
      | CERTIFICATE           | Common Health Entry Document for Animals (CHED-A) (as set out in Part 2, Section A of Annex II to Commission Implementing Regulation (EU) 2019/1715 (OJ L 261))                            | C640                         |
      | CERTIFICATE           | Check if you need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)                                                          | N853                         |
      | EXCEPTION             | CDS universal waiver                                                                                                                                                                       | 999L                         |
      | EXCEPTION             | If your goods are for scientific or research usage or for use as diagnostic samples, then your goods are exempt.                                                                           | C084                         |
      | THRESHOLD_CERTIFICATE | If your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use and weigh less than 20 kilograms, then your goods are exempt. | Y058                         |
    Then the response includes the prohibition measure data under measure 277 with measure type series A
      | key         | value                                                                                                                                                                                                                                                                                                          |
      | measureType | PROHIBITIVE                                                                                                                                                                                                                                                                                                    |
      | legalAct    | A1907950                                                                                                                                                                                                                                                                                                       |
      | description | ## You are not allowed to import live and chilled bivalve molluscs for human consumption from Turkey%0A%0AThis includes:%0A%0A- clams%0A- mussels%0A- scallops%0A- oysters%0A%0AThere is a ban on importing these goods.%0A%0A[Read the Regulation (EU 743/2103)](https://www.legislation.gov.uk/eur/2013/743) |

  Scenario: Prohibitions should not be returned in response for a commodity which does not have a prohibition
    When I call the measures API for commodity code 1006101000 with trade type IMPORT and origin country code CN and destination country code GB
    Then I should get a 200 response
    And the response should contain no prohibitions elements

  Scenario: Prohibitions should be returned in response for a commodity which has a prohibition along with condition based measures if any - exports
    Given the below measure type descriptions exist in the content repository
      | id | measureTypeId | descriptionOverlay   | tradeType | locale |
      | 1  | 478           | Export authorization | EXPORT    | EN     |
    And the below document code descriptions exist in the content repository
      | id | documentCode | descriptionOverlay                                                             | tradeType | locale |
      | 1  | Y929         | Your goods are not concerned by Regulation (EC) No 834/2007 (organic products) | EXPORT    | EN     |
      | 2  | X002         | Export licence: Dual use export authorisation more desc                        | EXPORT    | EN     |
      | 3  | Y999         | Goods for which an export licence is not required. Please use this             | EXPORT    | EN     |
    When I call the measures API for commodity code 4901100000 with trade type EXPORT and origin country code GB and destination country code BY
    Then the response includes the measures option data below under measure Export control with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay                                                                      | measureOptionCertificateCode |
      | CERTIFICATE       | Standard individual Export Licence: military goods and dual use goods subject to UK export controls. | 9104                         |
      | EXCEPTION         | Goods for which an export licence is not required. Please use this                                   | Y999                         |
    Then the response includes the measures option data below under measure Export authorization with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay                                    | measureOptionCertificateCode |
      | CERTIFICATE       | Export licence: Dual use export authorisation more desc            | X002                         |
      | EXCEPTION         | Goods for which an export licence is not required. Please use this | Y999                         |
    Then the response includes the measures option data below under measure Export control on cultural goods with measure type series B and measure type RESTRICTIVE
      | measureOptionType | measureOptionDescriptionOverlay                               | measureOptionCertificateCode |
      | CERTIFICATE       | Export licence "Cultural goods" (Regulation (EC) No 116/2009) | E012                         |
      | EXCEPTION         | Declared goods are not included in the list of cultural goods | Y903                         |
    Then the response includes the prohibition measure data under measure 278 with measure type series A
      | key         | value                                                                                                                                                                                                            |
      | measureType | PROHIBITIVE                                                                                                                                                                                                      |
      | legalAct    | X1906000                                                                                                                                                                                                         |
      | description | ## There are restrictions on the export of these goods to Belarus%0A%0AFor more information, see [Belarus sanctions guidance](https://www.gov.uk/government/publications/republic-of-belarus-sanctions-guidance) |

  Scenario: Call to measures API should return a 200 status code with http headers
    When I call the measures API for commodity code 1006107900 with trade type IMPORT and origin country code CN and destination country code GB
    Then I should get a 200 response
    And the response should contain the following header key and values
      | key                       | value                                                                                                                |
      | X-Content-Type-Options    | nosniff                                                                                                              |
      | Content-Security-Policy   | default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src 'self' blob: data:; |
      | Strict-Transport-Security | max-age=31536000; includeSubDomains                                                                                  |
    And The DIT API was called with commodity code 1006107900

  Scenario: Call to measures API with invalid import date format, should return a 400 status code
    When I call the measures API with trade date 09-09-2021 for commodity code 1006107900 with trade type IMPORT and origin country code CN and destination country code GB
    Then I should get a 400 response

  Scenario: Call to measures API valid trade date 2021-10-07 , should get measures specific to that date
    When I call the measures API with trade date 2021-10-07 for commodity code 0808108090 with trade type IMPORT and origin country code CN and destination country code GB
    Then I should get a 200 response
    Then the response includes the following measures
      | measureId | measureDescriptionOverlay                               | numberOfInstances |
      | 750       | Import control of organic products                      | 1                 |
      | 360       | Phytosanitary Certificate (import)                      | 1                 |
      | 355       | HMI Conformity Certificate (fruit and veg) issued in UK | 1                 |

 Scenario: Call to measures API valid trade date 2022-01-01, should get measures specific to that date
   When I call the measures API with trade date 2022-01-01 for commodity code 0808108090 with trade type IMPORT and origin country code CN and destination country code GB
   Then I should get a 200 response
   Then the response includes the following measures
     | measureId | measureDescriptionOverlay                               | numberOfInstances |
     | 750       | Import control of organic products                      | 1                 |
     | 355       | HMI Conformity Certificate (fruit and veg) issued in UK | 1                 |
