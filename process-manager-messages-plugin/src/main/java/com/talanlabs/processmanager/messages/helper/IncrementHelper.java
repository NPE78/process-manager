package com.talanlabs.processmanager.messages.helper;

import com.talanlabs.processmanager.messages.exceptions.SynchronizationException;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Calendar;
import java.util.concurrent.Semaphore;

public class IncrementHelper {

    private final FastDateFormat dateFormat;

    private final Semaphore semaphore = new Semaphore(1);

    private volatile Integer increment;

    private volatile Calendar calendar;

    private IncrementHelper() {
        super();

        this.calendar = Calendar.getInstance();
        this.increment = 0;

        dateFormat = FastDateFormat.getInstance("yyyyMMdd_HHmmss");

        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static IncrementHelper getInstance() {
        return IncrementHelper.SingletonHolder.instance;
    }

    private synchronized Integer getIncrement(Calendar now) {
        now.set(Calendar.MILLISECOND, 0);
        if (now.compareTo(calendar) <= 0) {
            increment++;
        } else {
            calendar = now;
            increment = 0;
        }
        return increment;
    }

    /**
     * Get a date signature with a unique increment associated to that date<br>
     * Format: yyyyMMdd_HHmmss_increment
     *
     * @return the computed string
     */
    public String getUniqueDate() {
        try {
            semaphore.acquire();
            Calendar now = Calendar.getInstance();
            return dateFormat.format(now.getTime()) + "_" + getIncrement(now);
        } catch (InterruptedException e) {
            throw new SynchronizationException(e);
        } finally {
            semaphore.release();
        }
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
        private static final IncrementHelper instance = new IncrementHelper();
    }
}
