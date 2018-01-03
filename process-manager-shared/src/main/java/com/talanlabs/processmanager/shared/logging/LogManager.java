package com.talanlabs.processmanager.shared.logging;

/**
 * A facade to get the real log service factory, customizable using setLogServiceFactory
 */
public final class LogManager {

    private static LogServiceFactory logServiceFactory;
    static {
        logServiceFactory = SLF4JLogServiceFactory.getInstance();
    }

    public static LogServiceFactory getLogServiceFactory() {
        return logServiceFactory;
    }

    public static void setLogServiceFactory(LogServiceFactory logServiceFactory) {
        LogManager.logServiceFactory = logServiceFactory;
    }

    public static LogService getLogService(String loggerName) {
        return getLogServiceFactory().getLogService(loggerName);
    }

    public static LogService getLogService(Class<?> loggerNameByClass) {
        return getLogServiceFactory().getLogService(loggerNameByClass);
    }
}
