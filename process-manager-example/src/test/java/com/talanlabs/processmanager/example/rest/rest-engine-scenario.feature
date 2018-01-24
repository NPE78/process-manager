Feature: Rest Engine feature
  We create an engine, add REST Addon
  We then add a REST agent and test it

  Background:
    Given REST engine is created
    And REST addon is created and added to the engine
    And REST agents are created and registered
    And REST dispatcher is created and registered
    And REST engine is initialized

  Scenario: Calling synchronous REST api
    When a synchronous url is called with hello param
    Then the status should be 200
    And the content should be hello

  Scenario: Calling asynchronous REST api
    When an asynchronous url is called
    Then the status should be 200
