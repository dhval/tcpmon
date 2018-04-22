package org.apache.dhval.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DateUtils.class);
    static DateFormat formatYYYYMMMDD = new SimpleDateFormat( "yyyy-MM-dd" );
    static DateFormat formatDateTime = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
    static  SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
    static {
        formatYYYYMMMDD.setLenient(false);
    }

    public static Date parse(String source)  {
        try {
            return formatDateTime.parse(source);
        } catch (ParseException p) {
            LOG.warn(source, p);
        }
        return null;
    }

    public static String format(Date date) {
        if (date == null)
            return  "";
        return formatYYYYMMMDD.format(date);
    }

    public static String gmt() {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }



}
