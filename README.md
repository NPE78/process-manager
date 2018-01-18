Process Manager
==============================

[![pipeline status](https://gitlab.talanlabs.com/nicolas-poste/process-manager/badges/master/pipeline.svg)](https://gitlab.talanlabs.com/nicolas-poste/process-manager/commits/master)
[![coverage report](https://gitlab.talanlabs.com/nicolas-poste/message-manager/badges/master/coverage.svg)](https://gitlab.talanlabs.com/nicolas-poste/message-manager/commits/master)

## Architecture
The Process Manager project is composed of many modules

### process-manager-shared

API and interfaces

### process-manager-server

Implementation of a the process manager engine

### process-manager-messages-plugin

Useful classes to manage files, using injectors, default agents and triggers.


## Usage

Use `ProcessManager.getInstance().createEngine(String uuid, File errorPath)` to build a new instance of a process manager.

You can add addons to an engine by using `Engine.addAddon(IEngineAddon engineAddon)`  
After creating the engine and adding agents to it, use `engine#activateChannels()`

To shutdown the whole engine, use `engine#shutdown` or `ProcessManager.getInstance().shutdownEngine(String engineUuid)`

## File Agent

An abstract agent is available to monitor a folder and dispatch files to the agent, given a maximum number of working agents at the same time.  
See `AbstractImportAgent#register(String engineUuid, int maxWorking, long delay, File basePath)`

## Probe agents

Two probe agents are available :
- CronAgent, which uses a scheduling pattern
- HeartbeatAgent, which wakes up every X milliseconds

After creating a probe agent, use `agent#activate(String engineUuid)` to activate it and bind it to the given engine
