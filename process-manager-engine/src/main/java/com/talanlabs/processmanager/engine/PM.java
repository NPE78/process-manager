package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.shared.exceptions.BaseEngineCreationException;
import com.talanlabs.processmanager.shared.Engine;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ProcessManager singleton
 */
public class PM {

    private final Map<String, Engine> engineMap;

    private PM() {
        engineMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns the Process Manager (singleton instance)
     */
    public static PM get() {
        return PM.SingletonHolder.instance;
    }

    /**
     * Handle a message on a channel of given engine
     *
     * @param engineUuid  the uuid of the engine
     * @param channelName the channel to send the message to
     * @param message     the message
     */
    public static void handle(String engineUuid, String channelName, Serializable message) {
        getEngine(engineUuid).handle(channelName, message);
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
     *
     * @param uuid the unique uuid of the engine
     * @return true if the engine has stop, false otherwise
     */
    public boolean shutdownEngine(String uuid) {
        synchronized (engineMap) {
            Engine engine = engineMap.get(uuid);
            if (engine != null) {
                engine.shutdown();
            }
            engineMap.remove(uuid);
            return engine != null;
        }
    }

    /* package protected */ void removeEngine(String engineUuid) {
        synchronized (engineMap) {
            engineMap.remove(engineUuid);
        }
    }

    /**
     * Returns the engine associated to the given uuid
     */
    public static Engine getEngine(String uuid) {
        return get().getEngineInternal(uuid);
    }

    private Engine getEngineInternal(String uuid) {
        synchronized (engineMap) {
            return engineMap.get(uuid);
        }
    }

    /**
     * Sécurité anti-désérialisation
     */
    private Object readResolve() {
        return get();
    }

    /**
     * Holder
     */
    private static final class SingletonHolder {
        private static final PM instance = new PM();
    }
}
