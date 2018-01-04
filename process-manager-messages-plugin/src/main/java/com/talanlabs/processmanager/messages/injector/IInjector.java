package com.talanlabs.processmanager.messages.injector;

import java.io.File;

/**
 * An interface for injectors
 *
 * @author Nicolas P
 */
public interface IInjector {

    /**
     * The name of the injector (and also agent)
     */
    String getName();

    /**
     * Inject a new singular message (file is optional)
     */
    void injectMessage(String content, File file);

    /**
     * Delay of refresh
     */
    long getDelay();

    /**
     * Get the working directory
     */
    File getWorkDir();

}
