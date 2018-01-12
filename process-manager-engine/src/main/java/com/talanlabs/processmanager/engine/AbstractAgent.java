package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.Agent;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.PluggableChannel;
import com.talanlabs.processmanager.shared.exceptions.AgentException;

/**
 * The most simple agent which helps to build a processing channel and bind it to an engine
 */
public abstract class AbstractAgent implements Agent {

    private final String name;

    private String engineUuid;

    public AbstractAgent(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    /**
     * Register the agent to the engine without creating a processing channel
     */
    public final void register(String engineUuid) {
        if (engineUuid == null) {
            throw new AgentException("You must provide an engineUuid");
        }
        if (this.engineUuid != null && !this.engineUuid.equals(engineUuid)) {
            throw new AgentException("This agent (" + getName() + ") is already bound to an engine: " + this.engineUuid);
        }
        this.engineUuid = engineUuid;
    }

    /**
     * Register the agent to the engine and create a processing channel
     */
    public void register(String engineUuid, int maxWorking) {
        register(engineUuid);
        if (maxWorking <= 0) {
            throw new AgentException("The max working must be an integer greater than 0");
        }

        Engine engine = getEngine(engineUuid);
        PluggableChannel pluggableChannel = new ProcessingChannel(name, maxWorking, getAgent());
        engine.plugChannel(pluggableChannel);
    }

    private Engine getEngine(String engineUuid) {
        Engine engine = ProcessManager.getEngine(engineUuid);
        if (engine == null) {
            throw new AgentException("The engine " + engineUuid +" does not exist");
        }
        return engine;
    }

    /**
     * Unregister from the current engine and stop the channel
     */
    public final void unregister() {
        ProcessManager.getEngine(engineUuid).unplugChannel(getName());
        this.engineUuid = null;
    }

    protected Agent getAgent() {
        return this;
    }
}
