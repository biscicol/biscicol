package util;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * Created by IntelliJ IDEA.
 * User: biocode
 * Date: Aug 12, 2011
 * Time: 3:31:29 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BSCDate {

    public static DateTime parseDate(String dateString) {
        if (dateString == null || dateString.equals("")) {
            return null;
        }
        // TODO: many of the input dates could be malformed w/ respect to time, need to standardize this here
        dateString = dateString.replace(" ", "T"); // Assume a space delimits the Time
        DateTime date = new DateTime(dateString);
        return date;
    }

    public static void main(String args[]) {
        //System.out.println(parseDate("2010-07-06 00:00:00").minus(Period.years(1)));
        //System.out.println(lastDay());
    }

    public static DateTime now() {
        return new DateTime();
    }

    public static DateTime lastDay() {
        return now().minus(Period.days(1));
    }

    public static DateTime lastWeek() {
        return now().minus(Period.weeks(1));
    }

    public static DateTime lastMonth() {
        return now().minus(Period.months(1));
    }

    public static DateTime lastYear() {
        return now().minus(Period.years(1));
    }

    public static String formatDate(DateTime dt) {
	    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        return dt != null ? dt.toString(fmt) : null;
    }

    /**
     *     Convenience method for comparing dates
      */
    public static boolean isDateBefore(DateTime date, DateTime dateFilter) {
    	return dateFilter != null && (date == null || date.isBefore(dateFilter));
    }

}