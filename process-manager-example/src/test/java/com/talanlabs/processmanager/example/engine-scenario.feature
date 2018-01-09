Feature: Engine feature
  We create an engine, add TriggerEngine and Gate Factory addons.
  We then add an agent which listens to a folder

Background:
  Given engine is created
  And agent is created and registered
  And engine is initialized

Scenario: Injecting a file which is correct
  When a valid file is received
  Then the file should be in the accepted folder
  Then shutdown the engine

Scenario: Injecting a file which is invalid
  When an invalid file is received
  Then the file should be in the rejected folder
  Then shutdown the engine