package com.talanlabs.processmanager.rest;

import com.talanlabs.processmanager.engine.EngineAddon;
import com.talanlabs.processmanager.rest.exceptions.RestServerException;
import io.javalin.ApiBuilder;
import io.javalin.Javalin;

import java.util.HashMap;
import java.util.Map;

public final class RestAddon extends EngineAddon<RestAddon> {

    private final Map<String, IRestDispatcher> routeMap;

    private final Javalin javalin;

    private RestAddon(String engineUuid) {
        super(RestAddon.class, engineUuid);

        routeMap = new HashMap<>();

        javalin = Javalin.create();
    }

    public static RestAddon register(String engineUuid) {
        return new RestAddon(engineUuid).registerAddon();
    }

    public void start(int port) {
        javalin.port(port).start();
    }

    public void bindDispatcher(IRestDispatcher restDispatcher) {
        synchronized (routeMap) {
            if (routeMap.containsKey(restDispatcher.getName())) {
                throw new RestServerException("The route " + restDispatcher.getName() + " exists already!");
            } else {
                javalin.routes(() -> ApiBuilder.path(restDispatcher.getName(), restDispatcher));
                routeMap.put(restDispatcher.getName(), restDispatcher);
            }
        }
    }

    @Override
    public void disconnectAddon() {
        routeMap.values().forEach(IRestDispatcher::clear);
        routeMap.clear();
        javalin.stop();
    }
}
