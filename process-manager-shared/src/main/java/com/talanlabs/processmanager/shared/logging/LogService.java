package com.talanlabs.processmanager.shared.logging;

import java.text.MessageFormat;
import java.util.function.Supplier;

/**
 * Pour plus d'informations, voir <a href="http://thecodersbreakfast.net/public/2017-04-10-devoxxfr-log-me-tender/LMT-DevoxxFR-2017.pdf">ce lien</a>
 */
public interface LogService {

    void log(LogLevel level, Supplier<String> message, Throwable exception);

    default void trace(Supplier<Object> message) {
        trace(() -> String.valueOf(message.get()), (Throwable) null);
    }

    default void trace(Supplier<String> message, Throwable exception) {
        log(LogLevel.TRACE, message, exception);
    }

    default void debug(Supplier<Object> message) {
        debug(() -> String.valueOf(message.get()), (Throwable) null);
    }

    default void debug(Supplier<String> message, Object... args) {
        debug(() -> MessageFormat.format(message.get(), args), (Throwable) null);
    }

    default void debug(Supplier<String> message, Throwable exception) {
        log(LogLevel.DEBUG, message, exception);
    }

    default void info(Supplier<String> message, Object... args) {
        info(() -> MessageFormat.format(message.get(), args), (Throwable) null);
    }

    default void info(Supplier<String> message, Throwable exception) {
        log(LogLevel.INFO, message, exception);
    }

    default void warn(Supplier<String> message, Object... args) {
        warn(() -> MessageFormat.format(message.get(), args), (Throwable) null);
    }

    default void warn(Supplier<String> message, Throwable exception) {
        log(LogLevel.WARNING, message, exception);
    }

    default void error(Supplier<String> message, Object... args) {
        error(() -> MessageFormat.format(message.get(), args), (Throwable) null);
    }

    default void error(Supplier<String> message, Throwable exception, Object... args) {
        error(() -> MessageFormat.format(message.get(), args), exception);
    }

    default void error(Supplier<String> message, Throwable exception) {
        log(LogLevel.ERROR, message, exception);
    }

    enum LogLevel {
        TRACE, DEBUG, INFO, WARNING, ERROR
    }
}
