package patrick.fuscoe.remindmelater.ui.main;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import patrick.fuscoe.remindmelater.models.ToDoGroup;

public class ToDoGroupsAdapter extends RecyclerView.Adapter<ToDoGroupsAdapter.ToDoGroupsViewHolder> {

    private List<ToDoGroup> toDoGroupList;


    public static class ToDoGroupsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView viewToDoCard;


        public ToDoGroupsViewHolder(View toDoGroupsView)
        {
            super(toDoGroupsView);
        }

    }

}
