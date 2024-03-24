package ru.mazhanchiki.severstal.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static Long getTimestamp(String input, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(input);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            return timestamp.getTime();
        } catch (ParseException e) {
            return null;
        }
    }
}
