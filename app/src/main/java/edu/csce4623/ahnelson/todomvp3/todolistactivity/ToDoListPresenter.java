package edu.csce4623.ahnelson.todomvp3.todolistactivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.List;

import edu.csce4623.ahnelson.todomvp3.addcreateeditactivity.AddCreateEditActivity;
import edu.csce4623.ahnelson.todomvp3.alarmnotification.AlarmNotification;
import edu.csce4623.ahnelson.todomvp3.data.ToDoItem;
import edu.csce4623.ahnelson.todomvp3.data.ToDoItemRepository;
import edu.csce4623.ahnelson.todomvp3.data.ToDoListDataSource;

/**
 * ToDoListPresenter -- Implements the Presenter interface from ToDoListContract Presenter
 */
public class ToDoListPresenter implements ToDoListContract.Presenter {

    //Data repository instance
    //Currently has a memory leak -- need to refactor context passing
    private static ToDoItemRepository mToDoItemRepository;
    //View instance
    private final ToDoListContract.View mToDoItemView;

    // Integer request codes for creating or updating through the result method
    private static final int CREATE_TODO_REQUEST = 0;
    private static final int UPDATE_TODO_REQUEST = 1;

    /**
     * ToDoListPresenter constructor
     * @param toDoItemRepository - Data repository instance
     * @param toDoItemView - ToDoListContract.View instance
     */
    public ToDoListPresenter(@NonNull ToDoItemRepository toDoItemRepository, @NonNull ToDoListContract.View toDoItemView){
        mToDoItemRepository = toDoItemRepository;
        mToDoItemView = toDoItemView;
        //Make sure to pass the presenter into the view!
        mToDoItemView.setPresenter(this);
    }

    @Override
    public void start(){
        //Load all toDoItems
        loadToDoItems();
    }


    /**
     * Create a new ToDoItem to store in the SQLite DB
     */
    @Override
    public void addNewToDoItem() {
        //Create stub ToDoItem with temporary data
        ToDoItem item = new ToDoItem();
        item.setTitle("Existing Title");
        item.setContent("Existing Content");
        item.setCompleted(false);
        item.setDueDate(0);
        item.setId(-1);
        //Show AddEditToDoItemActivity with a create request and temporary item
        Log.d("ToDoListPresenter", "Add ToDoItem");
        mToDoItemView.showAddEditToDoItem(item,CREATE_TODO_REQUEST);
    }

    @Override
    public void showExistingToDoItem(ToDoItem item) {
        //Show AddEditToDoItemActivity with a edit request, passing through an item
       Log.d("ToDoListPresenter", "Show Existing ToDoItem");
       mToDoItemView.showAddEditToDoItem(item,UPDATE_TODO_REQUEST);
    }

    /**
     * Callback for startActivityForResult handles results that are okay and not okay
     * @param requestCode -- Integer code identifying whether it was an update or edit call
     * @param resultCode -- Integer code identifying the result from the Intent
     * @param item -- ToDoItem returned from the AddEditToDoItemActivity
     */
    @Override
    public void result(int requestCode, int resultCode, ToDoItem item) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CREATE_TODO_REQUEST){
                createToDoItem(item);
            }else if(requestCode == UPDATE_TODO_REQUEST){
                updateToDoItem(item);
            }else{
                Log.e("ToDoPresenter", "No such request!");
            }
        }
    }

    /**
     * Create ToDoItem in repository from ToDoItem and reload data
     * @param item - item to be placed in the data repository
     */
    private void createToDoItem(ToDoItem item){
        Log.d("ToDoListPresenter","Create Item");
        mToDoItemRepository.createToDoItem(item);
    }

    /**
     * Update ToDoItem in repository from ToDoItem and reload data
     * @param item -- ToDoItem to be updated in the ToDoItemRepository
     */
    @Override
    public void updateToDoItem(ToDoItem item){
        Log.d("ToDoListPresenter", "Update Item");
        mToDoItemRepository.saveToDoItem(item);
    }

    /**
     * Delete ToDoItem in repository from ToDoITem and reload data
     * @param id -- ToDoItem ID to be deleted in the ToDoItemRepository
     */
    @Override
    public void deleteToDoItem(Long id) {
        Log.d("ToDoListPresenter", "Delete Item");
        mToDoItemRepository.deleteToDoItem(id);
        mToDoItemView.cancelAlarm(id);
        loadToDoItems();
    }

    /**
     * loadToDoItems -- loads all items from ToDoItemRepository
     * Two callbacks -- success/failure
     */
    @Override
    public void loadToDoItems(){
        Log.d("ToDoListPresenter","Loading ToDoItems");
        mToDoItemRepository.getToDoItems(new ToDoListDataSource.LoadToDoItemsCallback() {
            @Override
            public void onToDoItemsLoaded(List<ToDoItem> toDoItems) {
                Log.d("PRESENTER","Loaded");
                mToDoItemView.showToDoItems(toDoItems);
            }

            @Override
            public void onDataNotAvailable() {
                Log.d("PRESENTER","Not Loaded");
            }
        });
    }
}
