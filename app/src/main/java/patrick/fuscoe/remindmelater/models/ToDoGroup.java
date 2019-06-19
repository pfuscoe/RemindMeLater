package patrick.fuscoe.remindmelater.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ToDoGroup {

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

    public ToDoGroup(String title, String iconName, boolean shared, String userId)
    {
        this.title = title;
        this.iconName = iconName;

        this.shared = shared;

        this.numPriorityOneItems = 0;
        this.totalItems = 0;

        this.subscribers = new String[]{userId};

        this.toDoItemArrayList = new ArrayList<>();
        this.toDoItems = new HashMap<>();
    }


    public void addToDoItem(ToDoItem toDoItem)
    {
        toDoItemArrayList.add(toDoItem);
        totalItems++;

        if (toDoItem.getPriority() == 1)
        {
            numPriorityOneItems++;
        }
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
}
