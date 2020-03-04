package patrick.fuscoe.remindmelater.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory class for setting up passing to do group id argument to live data observer
 * in ToDoItemListActivity
 */
public class ToDoGroupViewModelFactory implements ViewModelProvider.Factory {

    private String toDoGroupId;

    public ToDoGroupViewModelFactory(String toDoGroupId)
    {
        this.toDoGroupId = toDoGroupId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ToDoGroupViewModel(toDoGroupId);
    }
}
