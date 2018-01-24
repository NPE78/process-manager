Process Manager
==============================

[![pipeline status](https://gitlab.talanlabs.com/nicolas-poste/process-manager/badges/master/pipeline.svg)](https://gitlab.talanlabs.com/nicolas-poste/process-manager/commits/master)
[![coverage report](https://gitlab.talanlabs.com/nicolas-poste/message-manager/badges/master/coverage.svg)](https://gitlab.talanlabs.com/nicolas-poste/message-manager/commits/master)

## Architecture
The Process Manager project is composed of many modules, some of which are plugins (addons)

### process-manager-shared

API and interfaces

### process-manager-server

Implementation of a the process manager engine

### process-manager-messages-plugin

Useful classes to manage files, using injectors, default agents and triggers.

### process-manager-rest-plugin

Useful classes to dispatch REST api to agents.


## Usage

Use `PM.get().createEngine(String uuid, File errorPath)` to build a new instance of a process manager.

You can add any number of addons to an engine by using `Engine.addAddon(IEngineAddon engineAddon)`  
After creating the engine and adding agents to it, use `engine#activateChannels()`

To shutdown the whole engine, use `engine#shutdown` or `PM.get().shutdownEngine(String engineUuid)`

## Message Addon

### File Agent

An abstract agent is available to monitor a folder and dispatch files to the agent, given a maximum number of working agents at the same time.  
See `AbstractImportAgent#register(String engineUuid, int maxWorking, long delay, File basePath)`

### Probe agents

Two probe agents are available :
- CronAgent, which uses a scheduling pattern
- HeartbeatAgent, which wakes up every X milliseconds

After creating a probe agent, use `agent#activate(String engineUuid)` to activate it and bind it to the given engine

## REST Addon

### Lightweight server

The REST addon launches a lightweight server (javalin) on a specified port. Use `RESTAddon#register(String engineUuid)` to use this addon with an engine.

### REST dispatcher

A REST dispatcher is associated to an URL, composed of the name of the dispatcher.  
Example : if the dispatcher name is "rest" and the port from the RESTAddon is 8080, then the URL would be `http://localhost:8080/rest`.  
Then, get, post, put, patch and delete verbs are dispatched to REST agents (override `#agentGet`, ...) considering their maximum working information.

### REST agents

REST agents use `#extract(Context context)` to extract the serializable information from the context they need to work.

REST agents can be synchronous or asynchronous, depending whether they return true or false with `#shouldLock()`.  
If they are synchronous, the `#doWork(Serializable message, Context context)` will expose the context to which information must be returned.
