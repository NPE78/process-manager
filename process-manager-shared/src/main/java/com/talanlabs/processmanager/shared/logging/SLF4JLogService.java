package com.talanlabs.processmanager.shared.logging;

import java.util.function.Supplier;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4JLogService implements LogService {

    private final Logger logger;

    SLF4JLogService(String loggerName) {
        this.logger = LoggerFactory.getLogger(loggerName);
    }

    SLF4JLogService(Class<?> loggerClass) {
        this.logger = LoggerFactory.getLogger(loggerClass);
    }

    @Override
    @Valid
    public void log(@NotNull LogLevel level, Supplier<String> message, Throwable exception) {
        switch (level) {
            case TRACE:
                logTrace(message, exception);
                break;
            case DEBUG:
                logDebug(message, exception);
                break;
            case INFO:
                logInfo(message, exception);
                break;
            case WARNING:
                logWarn(message, exception);
                break;
            case ERROR:
                logError(message, exception);
                break;
        }
    }

    private void logTrace(Supplier<String> message, Throwable exception) {
        if (logger.isTraceEnabled()) {
            logger.trace(message.get(), exception);
        }
    }

    private void logDebug(Supplier<String> message, Throwable exception) {
        if (logger.isDebugEnabled()) {
            logger.debug(message.get(), exception);
        }
    }

    private void logInfo(Supplier<String> message, Throwable exception) {
        if (logger.isInfoEnabled()) {
            logger.info(message.get(), exception);
        }
    }

    private void logWarn(Supplier<String> message, Throwable exception) {
        if (logger.isWarnEnabled()) {
            logger.warn(message.get(), exception);
        }
    }

    private void logError(Supplier<String> message, Throwable exception) {
        if (logger.isErrorEnabled()) {
            logger.error(message.get(), exception);
        }
    }
}
