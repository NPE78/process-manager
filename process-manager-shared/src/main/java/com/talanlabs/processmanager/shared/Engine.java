package com.talanlabs.processmanager.shared;

import com.talanlabs.processmanager.shared.exceptions.AddonAlreadyBoundException;
import java.util.List;
import java.util.Optional;

public interface Engine extends MessageHandler {

    /**
     * Returns the unique id of the engine
     */
    String getUuid();

    void plugChannel(PluggableChannel channel);

    void unplugChannel(String channelName);

    /**
     * Sets a engine listener to be notified
     */
    void setListener(EngineListener listener);

   void addAddon(IEngineAddon engineAddon) throws AddonAlreadyBoundException;

    <V extends IEngineAddon> Optional<V> getAddon(Class<V> addonClass);

    /**
     * Returns the list of channel slots
     */
    List<ChannelSlot> getChannelSlots();

    /**
     * Returns the list of plugged channels
     */
    List<PluggableChannel> getPluggedChannels();

    /**
     * Called when the process manager engine must shut down
     */
    void shutdown();

    /**
     * Activate all current channel slots. If a channel is plugged later, it has to be activated manually
     */
    void activateChannels();

    /**
     * Sets a channel available or not. To be available, a pluggable channel has to be plugged in
     */
    void setAvailable(String channelName, boolean available);

    /**
     * Returns the number of agents threads working right now on the given channel
     */
    int getNbWorking(String channelName);

    /**
     * Returns the number of agents threads waiting to be activated on the given channel
     */
    int getNbPending(String channelName);

    interface IEngineAddon {

        Class<? extends IEngineAddon> getAddonClass();

        void disconnectAddon();

    }
}
