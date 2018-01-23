Feature: Rest Engine feature
  We create an engine, add Rest Addon
  We then add a REST agent and test it

  Background:
    Given rest engine is created
    And rest addon is created and added to the engine
    And rest agents are created and registered
    And rest dispatcher is created and registered
    And rest engine is initialized

  Scenario: Calling synchronous REST api
    When a synchronous url is called with hello param
    Then the status should be 200
    And the content should be hello
    Then shutdown the rest engine

  Scenario: Calling asynchronous REST api
    When an asynchronous url is called
    Then the status should be 200
    Then shutdown the rest engine
