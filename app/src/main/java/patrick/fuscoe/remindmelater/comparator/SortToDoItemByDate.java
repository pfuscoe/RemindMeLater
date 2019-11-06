package patrick.fuscoe.remindmelater.comparator;

import java.util.Comparator;

import patrick.fuscoe.remindmelater.models.ToDoItem;

/**
 * Needed special comparator class for ToDoItem to allow alternate method of sorting.
 *
 * Only to do items marked done use this class.
 *
 * To do items that are not done are sorted first by priority, then alphabetically using
 * compareTo method in ToDoItem data model.
 */
public class SortToDoItemByDate implements Comparator<ToDoItem> {

    public int compare(ToDoItem a, ToDoItem b)
    {
        return b.getTimestamp().compareTo(a.getTimestamp());
    }

}
