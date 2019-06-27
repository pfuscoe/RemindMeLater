package patrick.fuscoe.remindmelater.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ToDoGroup {

    public static final String TAG = "patrick.fuscoe.remindmelater.ToDoGroup";

    private String id;
    private String title;
    private String iconName;
    private boolean shared;
    private int numPriorityOneItems;
    private int totalItems;

    private String[] subscribers;

    private ArrayList<ToDoItem> toDoItemArrayList;
    private Map<String, Object> toDoItems;
    private ToDoItem[] toDoItemsArray;


    public ToDoGroup() {

    }

    public ToDoGroup(String id, String title, String iconName, boolean shared, String userId)
    {
        this.id = id;
        this.title = title;
        this.iconName = iconName;
        this.shared = shared;
        this.numPriorityOneItems = 0;
        this.totalItems = 0;
        this.subscribers = new String[]{userId};

        this.toDoItemArrayList = new ArrayList<>();
        this.toDoItems = new HashMap<>();
    }

    public ToDoGroup(String id, String title, String iconName, boolean shared, int numPriorityOneItems, String[] subscribers, Map<String, Object> toDoItems)
    {
        this.id = id;
        this.title = title;
        this.iconName = iconName;
        this.shared = shared;
        this.numPriorityOneItems = numPriorityOneItems;
        this.subscribers = subscribers;
        this.toDoItems = toDoItems;

        this.totalItems = toDoItems.size();

        this.toDoItemArrayList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : toDoItems.entrySet())
        {
            String priorityString = entry.getValue().toString();
            Integer priority = Integer.valueOf(priorityString);
            Log.d(TAG, "ToDoItem key/value: " + entry.getKey() + ", " + priority);
            ToDoItem item = new ToDoItem(entry.getKey(), priority);
            toDoItemArrayList.add(item);
        }

        Collections.sort(toDoItemArrayList);

        // could add cleared item storage to more easily re-add done items
    }


    public void addToDoItem(ToDoItem toDoItem)
    {
        toDoItemArrayList.add(toDoItem);
        Collections.sort(toDoItemArrayList);
        totalItems++;

        if (toDoItem.getPriority() == 1)
        {
            numPriorityOneItems++;
        }

        // Update HashMap
        toDoItems.put(toDoItem.getItemName(), toDoItem.getPriority());
    }

    public void saveToDoItems()
    {
        Map<String, Object> toDoItemsTemp = new HashMap<>();

        for (int i = 0; i < toDoItemArrayList.size(); i++)
        {
            ToDoItem item = toDoItemArrayList.get(i);
            toDoItemsTemp.put(item.getItemName(), item.getPriority());
        }

        toDoItems = toDoItemsTemp;
    }

    /*
    public void saveToDoItemArray()
    {
        ToDoItem[] toDoItemArray = new ToDoItem[toDoItemArrayList.size()];
        toDoItems = toDoItemArrayList.toArray(toDoItemArray);
    }
    */

    /** Getters **/
    public String getTitle() {
        return title;
    }

    public String getIconName() {
        return iconName;
    }

    public boolean isShared() {
        return shared;
    }

    public int getNumPriorityOneItems() {
        return numPriorityOneItems;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public String[] getSubscribers() {
        return subscribers;
    }

    public ArrayList<ToDoItem> getToDoItemArrayList() {
        return toDoItemArrayList;
    }

    public ToDoItem[] getToDoItemsArray()
    {
        return toDoItemsArray;
    }

    public Map<String, Object> getToDoItems() {
        return toDoItems;
    }

    public String getId() {
        return id;
    }

    /** Setters **/
    public void setTitle(String title) {
        this.title = title;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public void setNumPriorityOneItems(int numPriorityOneItems) {
        this.numPriorityOneItems = numPriorityOneItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setToDoItemArrayList(ArrayList<ToDoItem> toDoItemArrayList) {
        this.toDoItemArrayList = toDoItemArrayList;
    }
}
