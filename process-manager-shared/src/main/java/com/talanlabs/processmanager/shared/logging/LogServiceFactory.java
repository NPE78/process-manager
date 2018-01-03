package com.talanlabs.processmanager.shared.logging;

public interface LogServiceFactory {

    default LogService getLogService(String loggerName) {
        return SLF4JLogServiceFactory.getInstance().getLogService(loggerName);
    }

    default LogService getLogService(Class<?> loggerClass) {
        return SLF4JLogServiceFactory.getInstance().getLogService(loggerClass);
    }
}
