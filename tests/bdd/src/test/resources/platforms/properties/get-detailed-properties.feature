Feature: Get detailed properties

  Background:
    Given an authenticated user

  Scenario: get the detail of a simple property
    Given an existing module named "A" with this template content
    """
    {{ simple-property }}
    """
    And an existing platform with this module
    And the platform has these valued properties
      | name            | value        |
      | simple-property | simple-value |
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name            | storedValue  | finalValue   | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | simple-property | simple-value | simple-value |              | false      | false      |         |         |                            | A                  | false     |

  Scenario: get the detail of a property with default value
    Given an existing module named "A" with this template content
    """
    {{ default-property | @default "toto" }}
    {{ overridden-default-property | @default "toto" }}
    """
    And an existing platform with this module
    And the platform has these valued properties
      | name                        | value            |
      | overridden-default-property | overriding-value |
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name                        | storedValue      | finalValue       | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | overridden-default-property | overriding-value | overriding-value | toto         | false      | false      |         |         |                            | A                  | false     |
      | default-property            |                  | toto             | toto         | false      | false      |         |         |                            | A                  | false     |

  Scenario: get the detail of a required property
    Given an existing module named "A" with this template content
    """
    {{ required-property | @required }}
    """
    And an existing platform with this module
    And the platform has these valued properties
      | name              | value          |
      | required-property | required-value |
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name              | storedValue    | finalValue     | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | required-property | required-value | required-value |              | true       | false      |         |         |                            | A                  | false     |

  Scenario: get the detail of a password property
    Given an existing module named "A" with this template content
    """
    {{ password-property | @password }}
    """
    And an existing platform with this module
    And the platform has these valued properties
      | name              | value    |
      | password-property | P4$$word |
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name              | storedValue | finalValue | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | password-property | P4$$word    | P4$$word   |              | false      | true       |         |         |                            | A                  | false     |

  Scenario: get the detail of a property with pattern
    Given an existing module named "A" with this template content
    """
    {{ pattern-property | @pattern "[0-9]" }}
    """
    And an existing platform with this module
    And the platform has these valued properties
      | name             | value |
      | pattern-property | 0     |
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name             | storedValue | finalValue | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | pattern-property | 0           | 0          |              | false      | false      | [0-9]   |         |                            | A                  | false     |

  Scenario: get the detail of a commented property
    Given an existing module named "A" with this template content
    """
    {{ commented-property | @comment "This is a comment" }}
    """
    And an existing platform with this module
    And the platform has these valued properties
      | name               | value |
      | commented-property | foo   |
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name               | storedValue | finalValue | defaultValue | isRequired | isPassword | pattern | comment           | referencedGlobalProperties | referencingModules | isNotUsed |
      | commented-property | foo         | foo        |              | false      | false      |         | This is a comment |                            | A                  | false     |

  Scenario: get the detail of a property referencing another property
    Given an existing module named "A" with this template content
    """
    {{ ref-property }}
    """
    And an existing platform with this module
    And the platform has these valued properties
      | name         | value              |
      | ref-property | {{ property-ref }} |
      | property-ref | ref-value          |
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name         | storedValue        | finalValue | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | ref-property | {{ property-ref }} | ref-value  |              | false      | false      |         |         |                            | A                  | false     |
      | property-ref | ref-value          | ref-value  |              | false      | false      |         |         |                            |                    | false     |

  Scenario: get the detail of properties referencing global properties
    Given an existing module named "A" with this template content
    """
    {{ global-property }}
    {{ ref-global-property }}
    """
    And an existing platform with this module
    And the platform has these valued properties
      | name                | value                 |
      | ref-global-property | {{ global-property }} |
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name                | storedValue           | finalValue   | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | ref-global-property | {{ global-property }} | global-value |              | false      | false      |         |         | global-property            | A                  | false     |
      | global-property     |                       | global-value |              | false      | false      |         |         | global-property            | A                  | false     |

  Scenario: get the detail of a property without value
    Given an existing module named "A" with this template content
    """
    {{ property-without-value }}
    """
    And an existing platform with this module
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name                   | storedValue | finalValue | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | property-without-value |             |            |              | false      | false      |         |         |                            | A                  | false     |

  Scenario: get the detail of a property that is not referenced in any template
    Given an existing module
    And an existing platform with this module
    And the platform has these valued properties
      | name              | value          |
      | not-used-property | not-used-value |
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name              | storedValue    | finalValue     | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | not-used-property | not-used-value | not-used-value |              | false      | false      |         |         |                            |                    | true      |

  Scenario: get the detail of a global property referencing another global property
    Given an existing platform
    And the platform has these global properties
      | name            | value            |
      | global-property | {{ global-ref }} |
      | global-ref      | global-value     |
    When I get the detailed properties of this platform
    Then the detailed global properties of this platform are
      | name            | storedValue      | finalValue   |
      | global-ref      | global-value     | global-value |
      | global-property | {{ global-ref }} | global-value |

  Scenario: get the detail of properties in multiple modules
    Given an existing module named "A" with this template content
    """
    {{ property-a }}
    {{ common-property }}
    """
    And an existing module named "B" with this template content
    """
    {{ property-b }}
    {{ common-property }}
    """
    And an existing platform with those modules
    And the module "A" has these valued properties
      | name            | value    |
      | property-a      | value-a  |
      | common-property | module-a |
    And the module "B" has these valued properties
      | name            | value    |
      | property-b      | value-b  |
      | common-property | module-b |
    When I get the detailed properties of this platform
    Then the detailed properties of module "A" are
      | name            | storedValue | finalValue | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | property-a      | value-a     | value-a    |              | false      | false      |         |         |                            | A                  | false     |
      | common-property | module-a    | module-a   |              | false      | false      |         |         |                            | A, B               | false     |
    And the detailed properties of module "B" are
      | name            | storedValue | finalValue | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | property-b      | value-b     | value-b    |              | false      | false      |         |         |                            | B                  | false     |
      | common-property | module-b    | module-b   |              | false      | false      |         |         |                            | A, B               | false     |

  Scenario: get the detail of nested properties
    Given an existing module named "A" with this template content
    """
    {{ property }}
    """
    And an existing platform with this module
    And the platform has these valued properties
      | name           | value                |
      | property       | {{ property-ref-a }} |
      | property-ref-a | {{ property-ref-b }} |
      | property-ref-b | property-value       |
    When I get the detailed properties of this module
    Then the detailed properties of this module are
      | name           | storedValue          | finalValue     | defaultValue | isRequired | isPassword | pattern | comment | referencedGlobalProperties | referencingModules | isNotUsed |
      | property-ref-b | property-value       | property-value |              | false      | false      |         |         |                            |                    | false     |
      | property       | {{ property-ref-a }} | property-value |              | false      | false      |         |         |                            | A                  | false     |
      | property-ref-a | {{ property-ref-b }} | property-value |              | false      | false      |         |         |                            |                    | false     |

