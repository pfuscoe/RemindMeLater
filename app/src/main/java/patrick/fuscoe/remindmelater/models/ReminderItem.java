package patrick.fuscoe.remindmelater.models;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;

public class ReminderItem {

    private String itemName;
    private Period recurrence;
    private LocalDate nextOccurance;
    private String category;
    private String description;

    //private Address address;

    private int daysAway;
    private boolean snoozed;
    private ArrayList<HistoryItem> historyItems;


    public ReminderItem() {

    }

    public ReminderItem(String itemName, Period recurrence, LocalDate nextOccurance, String category, String description)
    {
        this.itemName = itemName;
        this.recurrence = recurrence;
        this.nextOccurance = nextOccurance;
        this.category = category;
        this.description = description;

        updateDaysAway(nextOccurance);
        this.snoozed = false;
        this.historyItems = new ArrayList<>();
    }

    public void updateDaysAway(LocalDate nextOccurance)
    {
        LocalDate now = LocalDate.now();
        Period diff = Period.between(now, nextOccurance);
    }

    public void addToHistory(HistoryItem historyItem)
    {
        historyItems.add(historyItem);
    }

    /** Getters **/
    public String getItemName() {
        return itemName;
    }

    public Period getRecurrence() {
        return recurrence;
    }

    public LocalDate getNextOccurance() {
        return nextOccurance;
    }

    public String getCategory() {
        return category;
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

    public void setRecurrence(Period recurrence) {
        this.recurrence = recurrence;
    }

    public void setNextOccurance(LocalDate nextOccurance) {
        this.nextOccurance = nextOccurance;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSnoozed(boolean snoozed) {
        this.snoozed = snoozed;
    }

}
