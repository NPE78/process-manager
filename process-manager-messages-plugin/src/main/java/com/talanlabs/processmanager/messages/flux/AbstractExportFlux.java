package com.talanlabs.processmanager.messages.flux;

import com.talanlabs.processmanager.messages.model.SendInformation;
import com.talanlabs.processmanager.messages.model.annotation.Flux;
import java.io.File;
import java.io.Serializable;

abstract class AbstractExportFlux<E extends Serializable> extends AbstractFlux implements IExportFlux {

    private File file;

    private String extension;

    private E entity;

    private SendInformation sendInformation;

    public AbstractExportFlux() {
        super();

        Flux fluxAnnotation = this.getClass().getAnnotation(Flux.class);
        if (fluxAnnotation != null) {
            String ext = fluxAnnotation.extension();
            if (!ext.isEmpty()) {
                if (ext.charAt(0) != '.') {
                    ext = "." + ext;
                }
                setExtension(ext);
            }
        }
    }

    public final File getFile() {
        return file;
    }

    public final void setFile(File file) {
        setFilename(file.getName());
        this.file = file;
    }

    public String getExtension() {
        return extension;
    }

    private void setExtension(String extension) {
        this.extension = extension;
    }

    public final E getEntity() {
        return entity;
    }

    public final void setEntity(E entity) {
        this.entity = entity;
    }

    public final SendInformation getSendInformation() {
        if (this.sendInformation == null) {
            this.sendInformation = new SendInformation();
        }
        return this.sendInformation;
    }
}
