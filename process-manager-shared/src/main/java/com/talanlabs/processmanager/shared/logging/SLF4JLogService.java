package com.talanlabs.processmanager.shared.logging;

import java.util.function.Supplier;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
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
                if (logger.isTraceEnabled()) {
                    logger.trace(message.get(), exception);
                }
                break;
            case DEBUG:
                if (logger.isDebugEnabled()) {
                    logger.debug(message.get(), exception);
                }
                break;
            case INFO:
                if (logger.isInfoEnabled()) {
                    logger.info(message.get(), exception);
                }
                break;
            case WARNING:
                if (logger.isWarnEnabled()) {
                    logger.warn(message.get(), exception);
                }
                break;
            case ERROR:
                if (logger.isErrorEnabled()) {
                    logger.error(message.get(), exception);
                }
                break;
        }
    }
}
