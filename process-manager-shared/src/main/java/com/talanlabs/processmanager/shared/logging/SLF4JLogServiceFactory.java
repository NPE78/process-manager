package com.talanlabs.processmanager.shared.logging;

public class SLF4JLogServiceFactory implements LogServiceFactory {

    private SLF4JLogServiceFactory() {
    }

    public static SLF4JLogServiceFactory getInstance() {
        return SLF4JLogServiceFactory.SingletonHolder.instance;
    }

    @Override
    public LogService getLogService(String loggerName) {
        try {
            return new SLF4JLogService(Class.forName(loggerName));
        } catch (ClassNotFoundException e) {
            LogService logService = new SLF4JLogService(loggerName);
            logService.warn(() -> "Class of {0} could not be found!", e, loggerName);
            return logService;
        }
    }

    @Override
    public LogService getLogService(Class<?> loggerClass) {
        return new SLF4JLogService(loggerClass);
    }

    /**
     * Sécurité anti-désérialisation
     */
    private Object readResolve() {
        return getInstance();
    }

    /**
     * Holder
     */
    private static final class SingletonHolder {
        private static final SLF4JLogServiceFactory instance = new SLF4JLogServiceFactory();
    }
}
