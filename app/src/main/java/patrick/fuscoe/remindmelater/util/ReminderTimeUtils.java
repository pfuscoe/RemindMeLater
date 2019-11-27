package patrick.fuscoe.remindmelater.util;

import android.util.Log;

import java.time.LocalDate;
import java.time.Period;

/**
 * Provides static time-related helper methods for Reminders
 */
public class ReminderTimeUtils {

    private static final String TAG = "patrick.fuscoe.remindmelater.ReminderTimeUtils";


    public static Period createRecurrencePeriod(int recurrenceNum, String recurrenceInterval)
    {
        switch (recurrenceInterval)
        {
            case "Days":
                return Period.ofDays(recurrenceNum);

            case "Weeks":
                return Period.ofWeeks(recurrenceNum);

            case "Months":
                return Period.ofMonths(recurrenceNum);

            case "Years":
                return Period.ofYears(recurrenceNum);

            default:
                return Period.ofDays(recurrenceNum);
        }
    }

    public static LocalDate calcNextOccurrenceFromRecurrence(Period recurrence)
    {
        int daysUntilNext = recurrence.getDays();
        int monthsUntilNext = recurrence.getMonths();
        int yearsUntilNext = recurrence.getYears();

        LocalDate now = LocalDate.now();
        Log.d(TAG, ": now: " + now.toString());

        LocalDate nextOccurrence = now.plusDays(daysUntilNext);
        nextOccurrence = nextOccurrence.plusMonths(monthsUntilNext);
        nextOccurrence = nextOccurrence.plusYears(yearsUntilNext);

        return nextOccurrence;
    }
}
