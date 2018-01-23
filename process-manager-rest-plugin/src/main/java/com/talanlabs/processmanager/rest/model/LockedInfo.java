package com.talanlabs.processmanager.rest.model;

import io.javalin.Context;

import java.util.concurrent.CountDownLatch;

public final class LockedInfo {

    private final CountDownLatch countDownLatch;
    private final Context context;

    private LockedInfo(CountDownLatch countDownLatch, Context context) {
        this.countDownLatch = countDownLatch;
        this.context = context;
    }

    public static LockedInfo of(Context context) {
        return new LockedInfo(new CountDownLatch(1), context);
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public long getLockedCount() {
        return countDownLatch.getCount();
    }

    public void countDown() {
        countDownLatch.countDown();
    }

    public Context getContext() {
        return context;
    }
}
