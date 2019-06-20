package patrick.fuscoe.remindmelater.models;

public class ToDoItem implements Comparable<ToDoItem> {

    private String itemName;
    private int priority;


    public ToDoItem() {

    }

    public ToDoItem(String itemName, int priority)
    {
        this.itemName = itemName;
        this.priority = priority;
    }


    @Override
    public int compareTo(ToDoItem o)
    {
        int priorityComp = o.getPriority() - this.getPriority();

        if (priorityComp != 0)
        {
            return priorityComp;
        }
        else
        {
            return o.getItemName().compareTo(this.getItemName());
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
