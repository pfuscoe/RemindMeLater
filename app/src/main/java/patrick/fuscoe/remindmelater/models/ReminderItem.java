package patrick.fuscoe.remindmelater.models;

import android.location.Address;

import java.util.ArrayList;
import java.util.Date;

public class ReminderItem {

    private String itemName;
    private int daysAway;

    private Recurrance recurrance;

    private Date nextOccurance;
    private String description;

    //private Address address;

    private boolean snoozed;
    private ArrayList<HistoryItem> historyItems;


    public ReminderItem(String itemName, int daysAway, Recurrance recurrance, Date nextOccurance, String description)
    {
        this.itemName = itemName;
        this.daysAway = daysAway;
        this.recurrance = recurrance;
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

    public Recurrance getRecurrance() {
        return recurrance;
    }

    public Date getNextOccurance() {
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

    public void setRecurrance(Recurrance recurrance) {
        this.recurrance = recurrance;
    }

    public void setNextOccurance(Date nextOccurance) {
        this.nextOccurance = nextOccurance;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSnoozed(boolean snoozed) {
        this.snoozed = snoozed;
    }

}
