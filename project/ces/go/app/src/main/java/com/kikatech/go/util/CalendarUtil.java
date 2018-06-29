package com.kikatech.go.util;

import java.util.Calendar;

/**
 * @author SkeeterWang Created on 2018/4/2.
 */

public class CalendarUtil {

    public static long daysBetweenTodayAnd(long targetDateTimestamp) {
        Calendar targetDate = Calendar.getInstance();
        targetDate.setTimeInMillis(targetDateTimestamp);
        return daysBetweenTodayAnd(targetDate);
    }

    private static long daysBetweenTodayAnd(Calendar targetDate) {
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis() - getTimeOffset());
        return daysBetween(targetDate, today);
    }

    public static long daysBetween(long startDateTimestamp, long endDateTimestamp) {
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startDateTimestamp);
        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(endDateTimestamp);
        return daysBetween(startDate, endDate);
    }

    private static long daysBetween(Calendar startDate, Calendar endDate) {
        Calendar sDate = getDatePart(startDate);
        Calendar eDate = getDatePart(endDate);
        if (sDate.after(eDate)) {
            return -1;
        }
        long daysBetween = 0;
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    private static Calendar getDatePart(Calendar calendar) {
        Calendar cal = Calendar.getInstance();                // get calendar instance
        cal.setTimeInMillis(calendar.getTimeInMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);                    // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                        // set minute in hour
        cal.set(Calendar.SECOND, 0);                        // set second in minute
        cal.set(Calendar.MILLISECOND, 0);                    // set millisecond in second
        return cal;                                  // return the date part
    }

    private static long getTimeOffset() {
        return 0; // TODO: get time offset from network time server
    }
}
