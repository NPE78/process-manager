Feature: Engine feature

Scenario: Injecting a file
Given engine is created and initialized
  When a file is received
  Then it should have been successfully integrated
