package com.talanlabs.processmanager.engine;

import com.talanlabs.processmanager.engine.handlereport.BaseDelayedHandleReport;
import com.talanlabs.processmanager.shared.ChannelSlot;
import com.talanlabs.processmanager.shared.DelayedHandleReport;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.EngineListener;
import com.talanlabs.processmanager.shared.HandleReport;
import com.talanlabs.processmanager.shared.PluggableChannel;
import com.talanlabs.processmanager.shared.exceptions.AddonAlreadyBoundException;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The default engine which is a singleton. See {@link ProcessManager#createEngine(String, File)} to create one
 */
/* package protected*/ final class BaseEngine implements Engine {

    private final String uuid;
    private final Map<String, ChannelSlot> channelSlots;
    private final LogService logService;
    private final File errorPath;
    private final Map<Class<? extends IEngineAddon>, IEngineAddon> addons;
    private EngineListener listener;

    /**
     * Maximum number of retained messages
     */
    private int maxMsgCount;

    BaseEngine(String uuid, File errorPath) {
        this.logService = LogManager.getLogService(getClass());

        this.uuid = uuid;
        this.errorPath = errorPath;

        logService.info(() -> "Creating BaseEngine {0}...", uuid);

        this.channelSlots = new ConcurrentHashMap<>();

        if (!this.errorPath.exists() && !this.errorPath.mkdirs()) {
            logService.warn(() -> "Couldn't create errorPath of base engine " + uuid);
        }

        this.listener = new DefaultEngineListener();

        this.addons = new HashMap<>();

        init();
    }

    private void init() {
        channelSlots.clear();
        maxMsgCount = 10;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setListener(EngineListener listener) {
        this.listener = listener;
    }

    @Override
    public void addAddon(IEngineAddon engineAddon) throws AddonAlreadyBoundException {
        synchronized (addons) {
            IEngineAddon addon = addons.get(engineAddon.getAddonClass());
            if (addon != null) {
                throw new AddonAlreadyBoundException(getUuid(), addon);
            }
            addons.put(engineAddon.getAddonClass(), engineAddon);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends IEngineAddon> Optional<V> getAddon(Class<V> addonClass) {
        return Optional.ofNullable((V) addons.get(addonClass));
    }

    @Override
    public boolean isAvailable(String channelName) {
        synchronized (channelSlots) {
            if (!channelSlots.containsKey(channelName)) {
                return false;
            }
            ChannelSlot channel = resolveChannelSlot(channelName);
            return channel != null && channel.getPluggedChannel() != null && channel.isAvailable();
        }
    }

    @Override
    public boolean isBusy(String channelName) {
        ChannelSlot channel = resolveChannelSlot(channelName);
        return channel != null && channel.getPluggedChannel() != null && channel.isBusy();
    }

    @Override
    public boolean isOverloaded(String channelName) {
        ChannelSlot channel = resolveChannelSlot(channelName);
        return channel != null && channel.getPluggedChannel() != null && channel.getPluggedChannel().isOverloaded();
    }

    @Override
    public void plugChannel(PluggableChannel channel) {
        logService.info(() -> "Plug channel " + channel.getName());

        ChannelSlot channelSlot = resolveChannelSlot(channel.getName());
        channelSlot.plugChannel(channel);
    }

    @Override
    public void unplugChannel(String channelName) {
        ChannelSlot channelSlot = resolveChannelSlot(channelName);
        channelSlot.unplugChannel();
    }

    @Override
    public HandleReport handle(String channelName, Serializable message) {
        HandleReport report = null;
        ChannelSlot channel = resolveChannelSlot(channelName);
        if (channel != null) {
            listener.notifyHandle(channelName, message);
            report = channel.acceptMessage(message);
        }
        return report;
    }

    @Override
    public void activateChannels() {
        synchronized (channelSlots) {
            for (ChannelSlot ch : channelSlots.values()) {
                ch.activate(getUuid());
            }
        }
    }

    @Override
    public String toString() {
        return "Base Engine " + uuid;
    }

    @Override
    public void setAvailable(String channelName, boolean available) {
        ChannelSlot channelSlot = resolveChannelSlot(channelName);
        channelSlot.setAvailable(available);
    }

    @Override
    public int getNbWorking(String channelName) {
        ChannelSlot channelSlot = resolveChannelSlot(channelName);
        return channelSlot.getNbWorking();
    }

    /**
     * Called by the send method to return the ChannelSlot plugged (or to be plugged) on a named channel
     *
     * @param channelName name of the channel.
     * @return ChannelSlot plugged or to be plugged to the channel.
     */
    private ChannelSlot resolveChannelSlot(String channelName) {
        synchronized (channelSlots) {
            ChannelSlot channelSlot = channelSlots.get(channelName);
            if (channelSlot == null) {
                channelSlot = buildChannelSlot(channelName);
                channelSlots.put(channelName, channelSlot);
            }
            return channelSlot;
        }
    }

    private ChannelSlot buildChannelSlot(String channelName) {
        // This channel slot could not be found, create one and store messages...
        logService.info(() -> "Couldn''t find {0} channel slot, creating one", channelName);

        ChannelSlot channelslot = new ChannelSlotImpl(channelName, maxMsgCount);
        channelSlots.put(channelName, channelslot);
        listener.notifyNewSlot(channelslot);
        return channelslot;
    }

    @Override
    public List<PluggableChannel> getPluggedChannels() {
        synchronized (channelSlots) {
            return channelSlots.values().stream().filter(ChannelSlot::isPlugged).map(ChannelSlot::getPluggedChannel).collect(Collectors.toList());
        }
    }

    @Override
    public void shutdown() {
        logService.info(() -> "BaseEngine {0} is shutting down...", uuid);
        if (channelSlots != null && channelSlots.size() > 0) {
            for (String key : channelSlots.keySet()) {
                ChannelSlot channelSlot = channelSlots.get(key);
                storeMessages(channelSlot);
                if (channelSlot.isPlugged()) {
                    channelSlot.getPluggedChannel().shutdown();
                }
            }
        }
        addons.values().forEach(IEngineAddon::disconnectAddon);

        logService.info(() -> "BaseEngine {0} has been shut down. There may still be messages waiting to be processed to completely shutdown", uuid);

        ProcessManager.getInstance().removeEngine(getUuid()); // to be sure the engine has been removed in the process manager also
    }

    private void storeMessages(ChannelSlot channelSlot) {
        if (!channelSlot.isPlugged() && channelSlot.getBufferedMessagesCount() > 0) {
            int cpt = 1;
            for (Serializable msg : channelSlot.getBufferedMessages()) {
                channelSlot.storeBufferedMessage(errorPath + "/" + channelSlot.getName(), msg, cpt++);
            }
        }
    }

    public class ChannelSlotImpl extends AbstractChannel implements ChannelSlot {
        private final List<Serializable> savedMessages;
        private PluggableChannel channel;
        private int maxMsgCount;

        ChannelSlotImpl(String name, int maxMsgCount) {
            super(name);
            savedMessages = new ArrayList<>();
            this.maxMsgCount = maxMsgCount;
        }

        @Override
        public synchronized HandleReport acceptMessage(Serializable message) {
            if (channel != null) {
                if (channel.isAvailable()) {
                    return channel.acceptMessage(message, this);
                }
            } else {
                // No channel plugged on this slot.
                // we could ask for network notification.
                logService.trace(() -> "There is no channel for " + getName());
            }
            saveMessage(message);
            return new BaseDelayedHandleReport(this);
        }

        @Override
        public boolean isAvailable() {
            return channel != null && channel.isAvailable();
        }

        @Override
        public void setAvailable(boolean available) {
            if (channel != null) {
                channel.setAvailable(available);
            }
        }

        @Override
        public int getNbWorking() {
            if (channel != null) {
                return channel.getNbWorking();
            }
            return -1;
        }

        private synchronized void saveMessage(Serializable message) {
            synchronized (savedMessages) {
                if (savedMessages.size() < maxMsgCount) {
                    listener.notifySlotBuffering(this, message);
                    savedMessages.add(message);
                } else if (!savedMessages.isEmpty()) {
                    listener.notifySlotTrashing(this, savedMessages.remove(0));

                    listener.notifySlotBuffering(this, message);
                    savedMessages.add(message);
                }
            }
        }

        @Override
        public synchronized void plugChannel(PluggableChannel channel) {
            this.channel = channel;
            listener.notifySlotPlug(this);
            if (channel.isAvailable()) {
                flushMessages(channel);
            }
        }

        private void flushMessages(PluggableChannel channel) {
            while (!savedMessages.isEmpty()) {
                Serializable msg = savedMessages.remove(0);
                listener.notifySlotFlushing(this, msg);
                HandleReport report = channel.acceptMessage(msg, this);
                if (report instanceof DelayedHandleReport) {
                    listener.notifySlotBuffering(this, msg);
                    savedMessages.add(0, msg);
                    break;
                }
            }
        }

        @Override
        public synchronized void unplugChannel() {
            this.channel = null;
            listener.notifySlotUnplug(this);
        }

        @Override
        public boolean isPlugged() {
            return channel != null;
        }

        @Override
        public PluggableChannel getPluggedChannel() {
            return channel;
        }

        @Override
        public boolean isLocal() {
            return channel == null || channel.isLocal();
        }

        @Override
        public int getBufferedMessagesCount() {
            return savedMessages.size();
        }

        public List<Serializable> getBufferedMessages() {
            return new ArrayList<>(savedMessages);
        }

        @Override
        public boolean activate(String engineUuid) {
            boolean activated = false;
            if (channel != null) {
                activated = channel.activate(engineUuid);
                if (activated) {
                    flushMessages(channel);
                }
            }
            return activated;
        }

        @Override
        public void storeBufferedMessage(String foldername, Serializable message, int cpt) {
            File folder = new File(foldername);
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (!created) {
                    logService.error(() -> "Folder {0} couldn't be created", folder.getAbsolutePath());
                    return;
                }
            }
            File stored = new File(folder, Long.toString(System.currentTimeMillis()) + "." + cpt + ".queued");
            try (FileOutputStream fis = new FileOutputStream(stored);
                 ObjectOutputStream oos = new ObjectOutputStream(fis)) {
                oos.writeObject(message);
            } catch (IOException ex) {
                logService.error(() -> "Error when storing buffered message for engine {0}", ex, uuid);
            }
        }

        @Override
        public boolean isBusy() {
            return channel != null && channel.isBusy();
        }
    }
}
