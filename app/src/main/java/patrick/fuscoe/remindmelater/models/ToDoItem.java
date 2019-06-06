package patrick.fuscoe.remindmelater.models;

public class ToDoItem {

    private String itemName;
    private int priority;


    public ToDoItem() {

    }

    public ToDoItem(String itemName, int priority)
    {
        this.itemName = itemName;
        this.priority = priority;
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
