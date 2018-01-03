package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.engine.exceptions.BaseEngineCreationException;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessManager {

    private final Map<String, Engine> engineMap;

    private ProcessManager() {
        engineMap = new ConcurrentHashMap<>();
    }

    public static ProcessManager getInstance() {
        return ProcessManager.SingletonHolder.instance;
    }

    /**
     * Build a process manager engine
     *
     * @param uuid      Unique uuid of the engine
     * @param errorPath Path where the remaining messages will be stored when shutting down
     * @return the newly created engine
     * @throws BaseEngineCreationException an exception can be thrown if a uuid is already used
     */
    public Engine createEngine(String uuid, File errorPath) throws BaseEngineCreationException {
        synchronized (engineMap) {
            if (engineMap.containsKey(uuid)) {
                throw new BaseEngineCreationException(String.format("Base engine %s created twice!", uuid));
            }
            Engine engine = new BaseEngine(uuid, errorPath);
            engineMap.put(uuid, engine);
            return engine;
        }
    }

    /**
     * Shuts down the engine corresponding to the given uuid
     * @param uuid the unique uuid of the engine
     * @return true if the engine has stop, false otherwise
     */
    public boolean shutdownEngine(String uuid) {
        synchronized (engineMap) {
            return engineMap.remove(uuid) != null;
        }
    }

    /**
     * Returns the engine associated to the given uuid
     */
    public Engine getEngine(String uuid) {
        synchronized (engineMap) {
            return engineMap.get(uuid);
        }
    }

    /**
     * Sécurité anti-désérialisation
     */
    private Object readResolve() {
        return getInstance();
    }

    /**
     * Holder
     */
    private static final class SingletonHolder {
        private static final ProcessManager instance = new ProcessManager();
    }
}
