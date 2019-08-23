package patrick.fuscoe.remindmelater.models;

import java.time.LocalDate;
import java.util.Calendar;

public class ReminderAlarmItem {

    private String nextOccurrence;
    private int broadcastId;
    private Calendar alarmCalendar;


    public ReminderAlarmItem()
    {

    }

    public ReminderAlarmItem(String nextOccurrence, int broadcastId, int hour, int minute)
    {
        this.nextOccurrence = nextOccurrence;
        this.broadcastId = broadcastId;

        buildAlarmCalendarObject(hour, minute);
    }

    private void buildAlarmCalendarObject(int hour, int minute)
    {
        LocalDate alarmDate = LocalDate.parse(nextOccurrence);
        int year = alarmDate.getYear();
        int month = alarmDate.getMonthValue();
        int day = alarmDate.getDayOfMonth();

        alarmCalendar = Calendar.getInstance();
        alarmCalendar.setTimeInMillis(System.currentTimeMillis());
        alarmCalendar.set(year, month - 1, day);
        alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
        alarmCalendar.set(Calendar.MINUTE, minute);
    }

    public String getNextOccurrence() {
        return nextOccurrence;
    }

    public int getBroadcastId() {
        return broadcastId;
    }

    public Calendar getAlarmCalendar() {
        return alarmCalendar;
    }

}
