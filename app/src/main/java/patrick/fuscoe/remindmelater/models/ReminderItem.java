package patrick.fuscoe.remindmelater.models;

import android.util.Log;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;

public class ReminderItem implements Comparable<ReminderItem> {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReminderItem";

    private String title;
    private String nextOccurrence;  // LocalDate String format
    private String category;
    //private int categoryIcon;
    private String categoryIconName;
    private String description;

    //private Address address;
    private int recurrenceNum;
    private String recurrenceInterval;

    private Period recurrence;
    private String recurrenceString;
    private int daysAway;
    private boolean isSnoozed;
    private ArrayList<HistoryItem> historyItems;


    public ReminderItem() {

    }

    public ReminderItem(String title, int recurrenceNum, String recurrenceInterval, String nextOccurrence,
                        String category, String categoryIconName, String description, boolean isSnoozed)
    {
        this.title = title;
        this.recurrenceNum = recurrenceNum;
        this.recurrenceInterval = recurrenceInterval;
        this.nextOccurrence = nextOccurrence;
        this.category = category;
        this.categoryIconName = categoryIconName;
        this.description = description;

        updateRecurrencePeriod();
        updateDaysAway(nextOccurrence);

        this.recurrenceString = recurrence.toString();
        this.isSnoozed = isSnoozed;
        this.historyItems = new ArrayList<>();

        //Log.d(TAG, ": Reminder Item Constructed");
    }

    @Override
    public int compareTo(ReminderItem o) {
        return this.getDaysAway() - o.getDaysAway();
    }

    public void updateDaysAway(String nextOccurrence)
    {
        LocalDate now = LocalDate.now();
        LocalDate next = LocalDate.parse(nextOccurrence);
        Period diff = Period.between(now, next);

        daysAway = diff.getDays();
    }

    public void updateRecurrencePeriod()
    {
        //Log.d(TAG, ": updateRecurrencePeriod called");

        switch (recurrenceInterval)
        {
            case "Days":
                recurrence = Period.ofDays(recurrenceNum);
                return;

            case "Weeks":
                recurrence = Period.ofWeeks(recurrenceNum);
                return;

            case "Months":
                recurrence = Period.ofMonths(recurrenceNum);
                return;

            case "Years":
                recurrence = Period.ofYears(recurrenceNum);
                return;
        }

        //Log.d(TAG, ": recurrenceString: " + recurrenceString);
    }

    public void addToHistory(HistoryItem historyItem)
    {
        historyItems.add(historyItem);
    }

    /** Getters **/
    public String getTitle() {
        return title;
    }

    public int getRecurrenceNum() {
        return recurrenceNum;
    }

    public String getRecurrenceInterval() {
        return recurrenceInterval;
    }

    public Period getRecurrence() {
        return recurrence;
    }

    public String getRecurrenceString() {
        return recurrenceString;
    }

    public String getNextOccurrence() {
        return nextOccurrence;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryIconName() {
        return categoryIconName;
    }

    public String getDescription() {
        return description;
    }

    public int getDaysAway() {
        return daysAway;
    }

    public boolean isSnoozed() {
        return isSnoozed;
    }

    public ArrayList<HistoryItem> getHistoryItems() {
        return historyItems;
    }


    /** Setters **/
    public void setTitle(String title) {
        this.title = title;
    }

    public void setRecurrenceNum(int recurrenceNum) {
        this.recurrenceNum = recurrenceNum;
    }

    public void setRecurrenceInterval(String recurrenceInterval) {
        this.recurrenceInterval = recurrenceInterval;
        updateRecurrencePeriod();
    }

    public void setNextOccurrence(String nextOccurrence) {
        this.nextOccurrence = nextOccurrence;
        updateDaysAway(nextOccurrence);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCategoryIconName(String categoryIconName) {
        this.categoryIconName = categoryIconName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSnoozed(boolean isSnoozed) {
        this.isSnoozed = isSnoozed;
    }

}
