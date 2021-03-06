package patrick.fuscoe.remindmelater.models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Data model for ToDoGroup. These objects represent data that the user considers To Do Lists.
 *
 * Holds individual to do items in both an ArrayList and HashMap and maintains data consistency.
 * This is done to simplify cloud storage syncing.
*/
public class ToDoGroup {

    public static final String TAG = "patrick.fuscoe.remindmelater.ToDoGroup";

    private String id;
    private String title;
    private String iconName;
    private boolean shared;
    private int numPriorityOneItems;
    private int numUnfinishedItems;

    private String[] subscribers;

    private ArrayList<ToDoItem> toDoItemArrayList;
    private Map<String, Object> toDoItems;


    public ToDoGroup() {

    }

    public ToDoGroup(String id, String title, String iconName, boolean shared, String userId)
    {
        this.id = id;
        this.title = title;
        this.iconName = iconName;
        this.shared = shared;
        this.numPriorityOneItems = 0;
        this.numUnfinishedItems = 0;
        this.subscribers = new String[]{userId};

        this.toDoItemArrayList = new ArrayList<>();
        this.toDoItems = new HashMap<>();
    }

    public ToDoGroup(String id, String title, String iconName, boolean shared,
                     String[] subscribers, Map<String, Object> toDoItems)
    {
        this.id = id;
        this.title = title;
        this.iconName = iconName;
        this.shared = shared;
        this.numPriorityOneItems = 0;
        this.numUnfinishedItems = 0;
        this.subscribers = subscribers;
        this.toDoItems = toDoItems;

        this.toDoItemArrayList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : toDoItems.entrySet())
        {
            String itemName = entry.getKey();
            Map<String, Object> toDoItemMap = (Map<String, Object>) entry.getValue();

            int priority = Math.toIntExact((long)toDoItemMap.get("priority"));
            Timestamp timestamp = (Timestamp) toDoItemMap.get("timestamp");
            boolean done = (boolean) toDoItemMap.get("done");

            ToDoItem item = new ToDoItem(itemName, priority, timestamp, done);
            toDoItemArrayList.add(item);

            if (!item.isDone())
            {
                this.numUnfinishedItems++;

                if (item.getPriority() == 1)
                {
                    this.numPriorityOneItems++;
                }
            }
        }
    }


    public void addToDoItem(ToDoItem toDoItem)
    {
        toDoItemArrayList.add(toDoItem);

        if (!toDoItem.isDone())
        {
            numUnfinishedItems++;

            if (toDoItem.getPriority() == 1)
            {
                numPriorityOneItems++;
            }
        }

        // Update HashMap
        toDoItems.put(toDoItem.getItemName(), String.valueOf(toDoItem.getPriority()));
    }

    public void removeToDoItem(ToDoItem toDoItem)
    {
        toDoItemArrayList.remove(toDoItem);

        // Update HashMap
        toDoItems.remove(toDoItem.getItemName());
    }

    public void updateToDoItem(ToDoItem oldToDoItem, ToDoItem updatedToDoItem)
    {
        int oldPriority = oldToDoItem.getPriority();
        int newPriority = updatedToDoItem.getPriority();

        if (oldPriority == 1 && newPriority != 1)
        {
            numPriorityOneItems--;
        }
        else if (oldPriority != 1 && newPriority == 1)
        {
            numPriorityOneItems++;
        }

        int position = toDoItemArrayList.indexOf(oldToDoItem);
        toDoItemArrayList.set(position, updatedToDoItem);

        // Update HashMap
        toDoItems.remove(oldToDoItem.getItemName());
        toDoItems.put(updatedToDoItem.getItemName(), String.valueOf(updatedToDoItem.getPriority()));
    }

    public void removeSubscriber(String subscriberId)
    {
        ArrayList<String> tempList = new ArrayList<>(Arrays.asList(subscribers));
        tempList.remove(subscriberId);
        subscribers = tempList.toArray(new String[0]);
    }

    public void increaseNumPriorityOneItems()
    {
        numPriorityOneItems++;
    }

    public void decreaseNumPriorityOneItems()
    {
        numPriorityOneItems--;
    }

    public void increaseNumUnfinishedItems()
    {
        numUnfinishedItems++;
    }

    public void decreaseNumUnfinishedItems()
    {
        numUnfinishedItems--;
    }

    /* Getters */
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

    public int getNumUnfinishedItems() {
        return numUnfinishedItems;
    }

    public String[] getSubscribers() {
        return subscribers;
    }

    public ArrayList<ToDoItem> getToDoItemArrayList() {
        return toDoItemArrayList;
    }

    public Map<String, Object> getToDoItems() {
        return toDoItems;
    }

    public String getId() {
        return id;
    }


    /* Setters */
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

    public void setNumUnfinishedItems(int numUnfinishedItems) {
        this.numUnfinishedItems = numUnfinishedItems;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setToDoItemArrayList(ArrayList<ToDoItem> toDoItemArrayList) {
        this.toDoItemArrayList = toDoItemArrayList;
    }
}
