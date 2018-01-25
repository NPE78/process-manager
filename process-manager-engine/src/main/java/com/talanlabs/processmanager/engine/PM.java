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
    private static PM get() {
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
     * @param engineUuid      Unique engineUuid of the engine
     * @param errorPath Path where the remaining messages will be stored when shutting down
     * @return the newly created engine
     * @throws BaseEngineCreationException an exception can be thrown if a engineUuid is already used
     */
    public static Engine createEngine(String engineUuid, File errorPath) throws BaseEngineCreationException {
        return get().createEngineInternal(engineUuid, errorPath);
    }

    private Engine createEngineInternal(String engineUuid, File errorPath) throws BaseEngineCreationException {
        synchronized (engineMap) {
            if (engineMap.containsKey(engineUuid)) {
                throw new BaseEngineCreationException(String.format("Base engine %s created twice!", engineUuid));
            }
            Engine engine = new BaseEngine(engineUuid, errorPath);
            engineMap.put(engineUuid, engine);
            return engine;
        }
    }

    /**
     * Shuts down the engine corresponding to the given engineUuid
     *
     * @param engineUuid the unique engineUuid of the engine
     * @return true if the engine has stop, false otherwise
     */
    public static boolean shutdownEngine(String engineUuid) {
        return get().shutdownEngineInternal(engineUuid);
    }

    private boolean shutdownEngineInternal(String engineUuid) {
        synchronized (engineMap) {
            Engine engine = engineMap.get(engineUuid);
            if (engine != null) {
                engine.shutdown();
            }
            engineMap.remove(engineUuid);
            return engine != null;
        }
    }



    /* package protected */ static void removeEngine(String engineUuid) {
        get().removeEngineInternal(engineUuid);
    }

    private void removeEngineInternal(String engineUuid) {
        synchronized (engineMap) {
            engineMap.remove(engineUuid);
        }
    }

    /**
     * Returns the engine associated to the given engineUuid
     */
    public static Engine getEngine(String engineUuid) {
        return get().getEngineInternal(engineUuid);
    }

    private Engine getEngineInternal(String engineUuid) {
        synchronized (engineMap) {
            return engineMap.get(engineUuid);
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
