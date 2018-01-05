package com.talanlabs.processmanager.messages.injector;

import com.talanlabs.processmanager.messages.flux.AbstractImportFlux;
import com.talanlabs.processmanager.messages.helper.IncrementHelper;
import com.talanlabs.processmanager.messages.model.annotation.Flux;
import com.talanlabs.processmanager.messages.trigger.event.FileTriggerEvent;
import com.talanlabs.processmanager.shared.logging.LogManager;
import com.talanlabs.processmanager.shared.logging.LogService;
import java.io.File;
import java.io.FileOutputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * An abstract injector used to import flux<br>
 * Use annotation {@link Flux} to define a root work directory
 */
public abstract class AbstractInjector<M extends AbstractImportFlux> extends AbstractMsgInjector implements IInjector {

    private final LogService logService;

    private final Class<M> fluxClass;

    private final String acceptedPath;

    private final String retryPath;

    private final String rejectedPath;

    /**
     * Builds an injector dedicated to managing flux of the given type
     * @param fluxClass the managed flux class
     * @param baseWorkdir the base work directory (parent of where the injector folder will be located)
     */
    public AbstractInjector(Class<M> fluxClass, String baseWorkdir) {
        super();

        logService = LogManager.getLogService(getClass());

        this.fluxClass = fluxClass;

        String parentWorkdir = StringUtils.appendIfMissing(baseWorkdir, File.separator);
        this.acceptedPath = parentWorkdir + getName() + File.separator + "accepted" + File.separator;
        this.retryPath = parentWorkdir + getName() + File.separator + "retry" + File.separator;
        this.rejectedPath = parentWorkdir + getName() + File.separator + "rejected" + File.separator;
        setWorkDir(new File(parentWorkdir + getName()));
    }

    /**
     * Name of the directory, also used for receiving messages using t_jms_binding
     *
     * @return directory name
     */
    @Override
    public final String getName() {
        Flux fluxAnnotation = fluxClass.getAnnotation(Flux.class);
        if (fluxAnnotation != null) {
            return fluxAnnotation.fluxCode();
        } else {
            logService.warn(() -> "There is no Flux annotation for {0}. You should consider providing one", fluxClass.getName());
        }
        return null;
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
     * Implements this method to tell the injector what to do with the given flux
     */
    protected abstract void handleFlux(M flux);

    public String getAcceptedPath() {
        return acceptedPath;
    }

    public String getRejectedPath() {
        return rejectedPath;
    }

    public String getRetryPath() {
        return retryPath;
    }

    protected final void rejectFlux(M flux, Exception e) {
        if (flux.getFile() == null) {
            String content = getFluxContent(flux);
            if (content != null) { // if content is null, it may come from recycling
                File file = new File(rejectedPath, getName() + "_" + IncrementHelper.getInstance().getUniqueDate() + ".xml");
                dumpContentToFile(content, file);
                flux.setFile(file);
            } else {
                logService.error(() -> "Null content");
            }
        }
        if (getDelay() > 0 && flux.getFile() != null) {
            retryFile(flux.getFile(), flux);
        }
        logService.error(() -> "EXCEPTION - Flux not played", e);
    }

    private void dumpContentToFile(String content, File file) {
        try (FileOutputStream ou = new FileOutputStream(file, false)) {
            dumpContent(content, ou);
        } catch (Exception e) {
            logService.error(() -> "EXCEPTION FILE", e);
        }
    }

    private void dumpContent(String content, FileOutputStream ou) {
        try {
            ou.write(content.getBytes("UTF-8"));
        } catch (Exception e) {
            logService.error(() -> "EXCEPTION WRITE", e);
        }
    }

    private String getFluxContent(M flux) {
        String content = null;
        try {
            content = flux.getContent();
        } catch (Exception ex) {
            logService.debug(() -> "Exception occured when trying to get flux content", ex);
        }
        return content;
    }

    private void retryFile(File file, M message) {
        File destination = new File(retryPath + File.separator + file.getName());
        boolean success = file.renameTo(destination);
        if (success) {
            message.setFile(destination);
        }
    }

    @Override
    public long getDelay() {
        return 20000;
    }

    public abstract M createFlux();

}
