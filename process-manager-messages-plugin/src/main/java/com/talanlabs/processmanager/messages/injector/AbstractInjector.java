package com.talanlabs.processmanager.messages.injector;

import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.messages.trigger.event.FileTriggerEvent;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import org.apache.commons.lang3.StringUtils;

/**
 * An abstract injector used to import flux<br>
 */
public abstract class AbstractInjector<M extends AbstractImportFlux> extends AbstractMsgInjector implements IInjector {

    private final LogService logService;

    private final String name;

    private final String acceptedPath;
    private final String retryPath;
    private final String rejectedPath;
    private final String archivePath;

    /**
     * Builds an injector dedicated to managing flux of the given type
     *
     * @param name   the name of the flux (which is also the name of the final folder). Must be unique
     * @param baseWorkdir the base work directory (parent of where the injector folder will be located)
     */
    public AbstractInjector(String name, String baseWorkdir) {
        super(baseWorkdir);

        logService = LogManager.getLogService(getClass());

        this.name = name;

        String workDir = StringUtils.appendIfMissing(baseWorkdir, File.separator) + name + File.separator;
        this.acceptedPath = workDir + "accepted" + File.separator;
        this.retryPath = workDir + "retry" + File.separator;
        this.rejectedPath = workDir + "rejected" + File.separator;
        this.archivePath = workDir + "archive" + File.separator;
        setWorkDir(new File(workDir));
    }

    @Override
    public final String getName() {
        return name;
    }

    /**
     * Called when a file is injected
     */
    @Override
    public final Object inject(FileTriggerEvent evt) {
        File file = evt.getAttachment();
        if (evt.isNewFileEvent() && file.getParentFile().equals(getWorkDir())) {
            try {
                injectMessage(null, file); // content will be fetched later (lazy loading)
            } catch (Exception e) {
                logService.error(() -> "Error when injecting file", e);
            }
        }
        return null;
    }

    @Override
    public final void injectMessage(String content, File file) {
        M flux = createFlux();
        flux.setContent(content);
        flux.setFile(file);
        handleFlux(flux);
    }

    /**
     * Implement this method to tell the injector what to do with the given flux
     */
    protected abstract void handleFlux(M flux);

    public final String getAcceptedPath() {
        return acceptedPath;
    }

    public final String getRejectedPath() {
        return rejectedPath;
    }

    public final String getRetryPath() {
        return retryPath;
    }

    public final String getArchivePath() {
        return archivePath;
    }

    @Override
    public long getDelay() {
        return 20000;
    }

    public abstract M createFlux();

}
