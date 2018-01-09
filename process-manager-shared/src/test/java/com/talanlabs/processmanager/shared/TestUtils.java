package com.talanlabs.processmanager.shared;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestUtils {

    private static File errorPath;
    static {
        try {
            File tempFile = File.createTempFile("baseEngineTest", "tmp");
            File tmpFolder = tempFile.getParentFile();
            errorPath = new File(tmpFolder, UUID.randomUUID().toString());
            errorPath.mkdir();

            tempFile.deleteOnExit();
            errorPath.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getErrorPath() {
        return errorPath;
    }

    public static void sleep(int ms) throws InterruptedException {
        new CountDownLatch(1).await(ms, TimeUnit.MILLISECONDS);
    }
}
