package patrick.fuscoe.remindmelater.models;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Data model that holds information on each reminder item
*/
public class ReminderItem implements Comparable<ReminderItem> {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReminderItem";

    private String title;
    private String nextOccurrence;  // LocalDate String format
    private String category;
    private String categoryIconName;
    private String description;

    private int recurrenceNum;
    private String recurrenceInterval;

    private Period recurrence;
    private String recurrenceString;
    private int daysAway;
    private boolean isRecurring;
    private boolean isSnoozed;
    private boolean isHibernating;
    private Map<String, String> history;


    public ReminderItem() {

    }

    public ReminderItem(String title, boolean isRecurring, int recurrenceNum, String recurrenceInterval, String nextOccurrence,
                        String category, String categoryIconName, String description, boolean isSnoozed,
                        boolean isHibernating, Map<String, String> history)
    {
        this.title = title;
        this.isRecurring = isRecurring;
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
        this.isHibernating = isHibernating;
        this.history = history;
    }

    @Override
    public int compareTo(ReminderItem o) {
        return this.getDaysAway() - o.getDaysAway();
    }

    public void updateDaysAway(String nextOccurrence)
    {
        LocalDate now = LocalDate.now();
        LocalDate next = LocalDate.parse(nextOccurrence);

        daysAway = (int) now.until(next, ChronoUnit.DAYS);
    }

    public void updateRecurrencePeriod()
    {
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
    }

    public void addToHistory(String dateString, String action)
    {
        history.put(dateString, action);
    }

    /* Getters */
    public String getTitle() {
        return title;
    }

    public boolean isRecurring() {
        return isRecurring;
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

    public boolean isHibernating() {
        return isHibernating;
    }

    public Map<String, String> getHistory() {
        return history;
    }

    /* Setters */
    public void setTitle(String title) {
        this.title = title;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public void setRecurrenceNum(int recurrenceNum) {
        this.recurrenceNum = recurrenceNum;
    }

    public void setRecurrenceInterval(String recurrenceInterval) {
        this.recurrenceInterval = recurrenceInterval;
        updateRecurrencePeriod();
        this.recurrenceString = recurrence.toString();
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

    public void setHibernating(boolean hibernating) {
        isHibernating = hibernating;
    }
}
