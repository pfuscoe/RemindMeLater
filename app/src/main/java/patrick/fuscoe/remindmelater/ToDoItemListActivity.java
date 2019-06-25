package patrick.fuscoe.remindmelater;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.ToDoItem;
import patrick.fuscoe.remindmelater.ui.main.ToDoFragment;
import patrick.fuscoe.remindmelater.ui.todoitem.ToDoItemListAdapter;

public class ToDoItemListActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.ToDoItemListActivity";


    private RecyclerView toDoItemListRecyclerView;
    private RecyclerView.Adapter toDoItemListAdapter;
    private RecyclerView.LayoutManager toDoItemListLayoutManager;

    private ToDoGroup toDoGroup;
    private List<ToDoItem> toDoItemList;


    private ToDoItemClickListener toDoItemClickListener = new ToDoItemClickListener() {
        @Override
        public void toDoItemClicked(View v, int position) {

            Log.d(TAG, ": To Do Item at pos: " + position + " clicked");

            // TODO: Setup ToDoItem click action

            /*
            String viewItemURLString = similarItemsList.get(position).getViewItemURLString();

            // might need try statement
            Uri page = Uri.parse(viewItemURLString);
            Intent intent = new Intent(Intent.ACTION_VIEW, page);
            startActivity(intent);
            */
        }
    };

    public interface ToDoItemClickListener {
        void toDoItemClicked(View v, int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_item_list);
        Toolbar toolbar = findViewById(R.id.toolbar_to_do_item_list);
        setSupportActionBar(toolbar);

        // Get To Do Group from intent
        Intent intent = getIntent();
        Gson gson = new Gson();
        Type dataType = new TypeToken<ToDoGroup>(){}.getType();

        String toDoGroupString = intent.getStringExtra(ToDoFragment.TO_DO_GROUP);
        toDoGroup = gson.fromJson(toDoGroupString, dataType);

        toDoItemList = toDoGroup.getToDoItemArrayList();

        // Setup RecyclerView
        toDoItemListRecyclerView = findViewById(R.id.view_to_do_item_list_recycler);
        toDoItemListRecyclerView.setHasFixedSize(true);

        toDoItemListLayoutManager = new LinearLayoutManager(this);
        toDoItemListRecyclerView.setLayoutManager(toDoItemListLayoutManager);

        toDoItemListAdapter = new ToDoItemListAdapter(toDoItemList, this, toDoItemClickListener);
        toDoItemListRecyclerView.setAdapter(toDoItemListAdapter);


        /*
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(toDoGroup.getTitle());
    }

    public void UpdateToDoItemListDisplay()
    {
        toDoItemListAdapter = new ToDoItemListAdapter(toDoItemList, this, toDoItemClickListener);
        toDoItemListRecyclerView.setAdapter(toDoItemListAdapter);

        toDoItemListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_add:
                Log.d(TAG, ": Add Button pressed");

            case R.id.menu_main_user_settings:
                Log.d(TAG, ": Menu item selected: " + item.getItemId());

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
