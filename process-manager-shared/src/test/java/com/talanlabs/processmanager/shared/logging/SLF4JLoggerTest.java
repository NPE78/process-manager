package com.talanlabs.processmanager.shared.logging;

import org.junit.Test;

public class SLF4JLoggerTest {

    @Test
    public void testSlf4JLogger() {
        LogService logService = LogManager.getLogService(getClass());

        logService.trace(() -> "Trace");
        logService.debug(() -> "Debug");
        logService.info(() -> "Info");
        logService.warn(() -> "Warn");
        logService.error(() -> "Error");
    }

    @Test
    public void testSlf4JLoggerWithClassString() {
        LogService logService = LogManager.getLogService(getClass().getName());

        logService.trace(() -> "Trace");
        logService.debug(() -> "Debug");
        logService.info(() -> "Info");
        logService.warn(() -> "Warn");
        logService.error(() -> "Error");
    }

    @Test
    public void testSlf4JLoggerWithString() {
        LogService logService = LogManager.getLogService("unknownClass");

        logService.trace(() -> "Trace");
        logService.debug(() -> "Debug");
        logService.info(() -> "Info");
        logService.warn(() -> "Warn");
        logService.error(() -> "Error");
    }
}
