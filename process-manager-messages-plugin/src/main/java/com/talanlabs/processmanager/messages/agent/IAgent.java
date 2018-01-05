package com.talanlabs.processmanager.messages.agent;

import com.talanlabs.processmanager.messages.flux.IFlux;
import com.talanlabs.processmanager.shared.Agent;

/**
 * Marker interfaces for agents which has to process M flux
 * @param <M> type of flux
 */
public interface IAgent<M extends IFlux> extends Agent {

    Class<M> getAgentClass();

}
