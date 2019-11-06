package patrick.fuscoe.remindmelater.models;

import com.google.firebase.Timestamp;

/**
 * Data model for individual to do items
*/
public class ToDoItem implements Comparable<ToDoItem> {

    private String itemName;
    private int priority;
    private Timestamp timestamp;
    private boolean done;


    public ToDoItem() {

    }

    public ToDoItem(String itemName, int priority)
    {
        this.itemName = itemName;
        this.priority = priority;
        this.timestamp = Timestamp.now();
        this.done = false;
    }

    public ToDoItem(String itemName, int priority, Timestamp timestamp, boolean done)
    {
        this.itemName = itemName;
        this.priority = priority;
        this.timestamp = timestamp;
        this.done = done;
    }


    @Override
    public int compareTo(ToDoItem o)
    {
        int priorityComp = this.getPriority() - o.getPriority();

        if (priorityComp != 0)
        {
            return priorityComp;
        }
        else
        {
            return this.getItemName().compareTo(o.getItemName());
        }
    }

    public String getItemName()
    {
        return this.itemName;
    }

    public int getPriority()
    {
        return this.priority;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public boolean isDone() {
        return this.done;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
