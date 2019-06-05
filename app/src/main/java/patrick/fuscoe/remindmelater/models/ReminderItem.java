package patrick.fuscoe.remindmelater.models;

import android.location.Address;

import java.util.ArrayList;
import java.util.Date;

public class ReminderItem {

    private String itemName;
    private int daysAway;

    // TODO: setup recurrance object
    //private Recurrance recurrance;

    private Date nextOccurance;
    private String description;

    //private Address address;

    private boolean snoozed;
    private ArrayList<HistoryItem> historyItems;


    //public ReminderItem(String itemName, int daysAway, )

}
