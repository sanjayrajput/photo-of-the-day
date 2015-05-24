package com.potd;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by sanjay.rajput on 11/05/15.
 */
public class Utils {

    public static Date getDateBeforeDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -days);
        return cal.getTime();
    }

    public static Date getDateAfterDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -days);
        return cal.getTime();
    }
}
