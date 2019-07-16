package patrick.fuscoe.remindmelater.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ReminderItem {

    private String itemName;
    private int daysAway;
    private Recurrence recurrence;
    private Calendar nextOccurance;
    private String description;

    //private Address address;

    private boolean snoozed;
    private ArrayList<HistoryItem> historyItems;


    public ReminderItem() {

    }

    public ReminderItem(String itemName, int daysAway, Recurrence recurrence, Calendar nextOccurance, String description)
    {
        this.itemName = itemName;
        this.daysAway = daysAway;
        this.recurrence = recurrence;
        this.nextOccurance = nextOccurance;
        this.description = description;

        this.snoozed = false;
        this.historyItems = new ArrayList<>();
    }

    public void addToHistory(HistoryItem historyItem)
    {
        historyItems.add(historyItem);
    }

    /** Getters **/
    public String getItemName() {
        return itemName;
    }

    public int getDaysAway() {
        return daysAway;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public Calendar getNextOccurance() {
        return nextOccurance;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSnoozed() {
        return snoozed;
    }

    public ArrayList<HistoryItem> getHistoryItems() {
        return historyItems;
    }


    /** Setters **/
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setDaysAway(int daysAway) {
        this.daysAway = daysAway;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    public void setNextOccurance(Calendar nextOccurance) {
        this.nextOccurance = nextOccurance;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSnoozed(boolean snoozed) {
        this.snoozed = snoozed;
    }

}
