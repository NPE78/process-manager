Process Manager
==============================

[![pipeline status](https://gitlab.talanlabs.com/nicolas-poste/process-manager/badges/master/pipeline.svg)](https://gitlab.talanlabs.com/nicolas-poste/process-manager/commits/master)
[![coverage report](https://gitlab.talanlabs.com/nicolas-poste/message-manager/badges/master/coverage.svg)](https://gitlab.talanlabs.com/nicolas-poste/message-manager/commits/master)

## Architecture
The Process Manager project is composed of two modules

### process-manager-shared

API and interfaces

### process-manager-server

Implementation of a the process manager engine


## Usage

Use `ProcessManager.createEngine(String uuid, File errorPath)` to build a new instance of a process manager.



