package patrick.fuscoe.remindmelater.models;

import com.google.firebase.Timestamp;

public class ToDoItem implements Comparable<ToDoItem> {

    private String itemName;
    private int priority;
    private Timestamp timestamp;
    private boolean isDone;


    public ToDoItem() {

    }

    public ToDoItem(String itemName, int priority)
    {
        this.itemName = itemName;
        this.priority = priority;
        this.timestamp = Timestamp.now();
        this.isDone = false;
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

}
