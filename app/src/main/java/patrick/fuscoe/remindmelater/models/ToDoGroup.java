package patrick.fuscoe.remindmelater.models;

import java.util.ArrayList;

public class ToDoGroup {

    private String title;
    private String iconName;
    private boolean shared;
    private int numPriorityOneItems;
    private int totalItems;

    private ArrayList<ToDoItem> toDoItemArrayList;
    private ToDoItem[] toDoItems;


    public ToDoGroup() {

    }

    public ToDoGroup(String title, String iconName, boolean shared)
    {
        this.title = title;
        this.iconName = iconName;
        this.shared = shared;

        this.numPriorityOneItems = 0;
        this.totalItems = 0;

        this.toDoItemArrayList = new ArrayList<>();
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

    public void saveToDoItemArray()
    {
        ToDoItem[] toDoItemArray = new ToDoItem[toDoItemArrayList.size()];
        toDoItems = toDoItemArrayList.toArray(toDoItemArray);
    }

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

    public ArrayList<ToDoItem> getToDoItemArrayList() {
        return toDoItemArrayList;
    }

    public ToDoItem[] getToDoItems()
    {
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
