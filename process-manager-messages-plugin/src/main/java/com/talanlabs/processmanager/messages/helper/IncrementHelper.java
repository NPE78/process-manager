package com.talanlabs.processmanager.messages.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class IncrementHelper {

    private final SimpleDateFormat simpleDateFormat;

    private Integer increment;

    private Calendar calendar;

    private IncrementHelper() {
        super();

        this.calendar = Calendar.getInstance();
        this.increment = 0;
        this.simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static IncrementHelper getInstance() {
        return IncrementHelper.SingletonHolder.instance;
    }

    private synchronized Integer getIncrement() {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.MILLISECOND, 0);
        if (now.compareTo(calendar) == 0) {
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
        return simpleDateFormat.format(calendar.getTime()) + "_" + getIncrement();
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
