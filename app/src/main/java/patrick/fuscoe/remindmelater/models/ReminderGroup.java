package patrick.fuscoe.remindmelater.models;

import java.util.ArrayList;

public class ReminderGroup {

    private String title;
    private String iconName;
    private int numItemsToday;
    private int numItemsSnoozed;
    private int numItemsTotal;

    private ArrayList<ReminderItem> reminderItems;


    public ReminderGroup(String title, String iconName)
    {
        this.title = title;
        this.iconName = iconName;

        this.numItemsToday = 0;
        this.numItemsSnoozed = 0;
        this.numItemsTotal = 0;

        this.reminderItems = new ArrayList<>();
    }


    public void addReminderItem(ReminderItem reminderItem)
    {
        reminderItems.add(reminderItem);
    }


    /** Getters **/
    public String getTitle() {
        return title;
    }

    public String getIconName() {
        return iconName;
    }

    public int getNumItemsToday() {
        return numItemsToday;
    }

    public int getNumItemsSnoozed() {
        return numItemsSnoozed;
    }

    public int getNumItemsTotal() {
        return numItemsTotal;
    }

    public ArrayList<ReminderItem> getReminderItems() {
        return reminderItems;
    }


    /** Setters **/
    public void setTitle(String title) {
        this.title = title;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public void setNumItemsToday(int numItemsToday) {
        this.numItemsToday = numItemsToday;
    }

    public void setNumItemsSnoozed(int numItemsSnoozed) {
        this.numItemsSnoozed = numItemsSnoozed;
    }

    public void setNumItemsTotal(int numItemsTotal) {
        this.numItemsTotal = numItemsTotal;
    }

}
