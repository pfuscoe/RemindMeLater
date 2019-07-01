package patrick.fuscoe.remindmelater.comparator;

import java.util.Comparator;

import patrick.fuscoe.remindmelater.models.ToDoItem;

public class SortToDoItemByDate implements Comparator<ToDoItem> {

    // Sort by most recent
    public int compare(ToDoItem a, ToDoItem b)
    {
        return b.getTimestamp().compareTo(a.getTimestamp());
    }

}
