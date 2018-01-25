Feature: Rest Engine feature
  We create an engine, add REST Addon
  We then add a REST agent and test it

  Background:
    Given REST engine is created
    And REST addon is created and added to the engine


  Scenario Outline: Calling synchronous REST api
    Given REST agents are created and registered
    And REST dispatcher is created and registered
    And REST engine is initialized
    When a synchronous url is called with <param> param
    Then the status should be <status>
    And the content should be <expectedAnswer>
    Examples:
      | param  | status | expectedAnswer |
      | hello  | 200    | hi there!      |
      | foo    | 200    | bar            |
      | teapot | 418    |                |
      | none   | 500    |                |


  Scenario: Calling asynchronous REST api
    Given REST agents are created and registered
    And REST dispatcher is created and registered
    And REST engine is initialized
    When an asynchronous url is called
    Then the status should be 200


  Scenario Outline: Calling all methods
    Given A dispatcher contains an agent for every method
    And REST engine is initialized
    When an asynchronous url is called with <method>
    Then the status should be 200

    Examples:
      | method |
      | get    |
      | post   |
      | put    |
      | patch  |
      | delete |

