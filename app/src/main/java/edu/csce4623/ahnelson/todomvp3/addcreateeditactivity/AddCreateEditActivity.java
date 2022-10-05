package edu.csce4623.ahnelson.todomvp3.addcreateeditactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.UUID;

import edu.csce4623.ahnelson.todomvp3.R;
import edu.csce4623.ahnelson.todomvp3.alarmnotification.AlarmNotification;
import edu.csce4623.ahnelson.todomvp3.data.ToDoItem;

public class AddCreateEditActivity extends AppCompatActivity {

    private Button btnSetAlarm;
    private CheckBox chkBoxDone;
    private EditText etItemTitle;
    private EditText etItemContent;
    private TextView tvDateAndTime;
    private static Calendar dateTime;
    AlarmManager almManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_create_edit);

        btnSetAlarm = findViewById(R.id.btnSetAlarm);
        chkBoxDone = findViewById(R.id.chkBoxDone);
        etItemTitle = findViewById(R.id.etItemTitle);
        etItemContent = findViewById(R.id.etItemBody);
        tvDateAndTime = findViewById(R.id.tvDateAndTime);
        dateTime = Calendar.getInstance();
        almManager = getAlarmManager();

        // Extras from Intent
        Bundle extras = getIntent().getExtras();
        final ToDoItem toDoItem = (ToDoItem) extras.getSerializable("ToDoItem");
        final int requestCode = (int) extras.getInt("requestCode");

        // Load values from DB if item exists already
        if(requestCode == 1) {
            chkBoxDone.setChecked(toDoItem.getCompleted());
            etItemTitle.setText(toDoItem.getTitle());
            etItemContent.setText(toDoItem.getContent());
            if(toDoItem.getDueDate() != 0) {
                tvDateAndTime.setText(android.text.format.DateFormat.format("MM/dd/yyyy h:mm a", toDoItem.getDueDate()));
            }
        }

        /**
         * OnClickListener for the date and time display in the bottom left of this activity
         * It will show the date picker and time picker and set the dateTime
         */
        tvDateAndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
                showTimePickerDialog();
            }
        });

        /**
         * OnClickListener for the btnSetAlarm
         * Saves or updates the ToDoItem in the database
         * Sets the alarm if there is one
         */
        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tvDateAndTime.getText().toString().contains("/")) {
                    // Wrap toDoItem in a bundle
                    // https://stackoverflow.com/questions/40480355/pass-serializable-object-to-pending-intent/40515978#40515978
                    Bundle bundle = new Bundle();
                    Long dueDate = Long.valueOf(0);
                    if(tvDateAndTime.getText() != "") {
                        dueDate = dateTime.getTimeInMillis();
                    }
                    updateToDoItem(toDoItem, etItemTitle.getText().toString(), etItemContent.getText().toString(), dueDate, chkBoxDone.isChecked());
                    bundle.putSerializable("ToDoItem", toDoItem);

                    Toast.makeText(getApplicationContext(),"Alarm will go off at " + android.text.format.DateFormat.format("MM/dd/yyyy h:mm a", dateTime.getTimeInMillis()), Toast.LENGTH_SHORT).show();

                    // Create Intent
                    Intent alarmNotificationIntent = new Intent(view.getContext(), AlarmNotification.class);
                    alarmNotificationIntent.putExtra("bundle", bundle);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(view.getContext(), 42, alarmNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Log.d("AddCreateEditActivity", "Alarm set for " + android.text.format.DateFormat.format("MM/dd/yyyy h:mm a", dateTime.getTimeInMillis()));
                    almManager.set(AlarmManager.RTC_WAKEUP, dateTime.getTimeInMillis(), alarmIntent);
                } else {
                    updateToDoItem(toDoItem, etItemTitle.getText().toString(), etItemContent.getText().toString(), (long) 0, chkBoxDone.isChecked());
                }

                // Finish the activity
                Intent intent = new Intent();
                intent.putExtra("ToDoItem", toDoItem);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    /**
     * Gets alarm manager based on the android version
     * @return AlarmManager Service
     */
    public AlarmManager getAlarmManager() {
        AlarmManager almManager;

        // Set Alarm Service
        if(Build.VERSION.SDK_INT >= 23) {
            almManager = getApplicationContext().getSystemService(AlarmManager.class);
        } else {
            almManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        }
        return almManager;
    }

    /**
     * Handles what happens when the user presses the back button when in this activity screen
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Updates the ToDoItem passed as a param based on values passed in
     * @param toDoItem
     * @param title
     * @param content
     * @param dueDate
     * @param completed
     */
    public void updateToDoItem(ToDoItem toDoItem, String title, String content, Long dueDate, Boolean completed) {
        if(title != null) {
            toDoItem.setTitle(title);
        }
        if(content != null) {
            toDoItem.setContent(content);
        }
        if(dueDate != null) {
            toDoItem.setDueDate(dueDate);
        }
        if(completed != null) {
            toDoItem.setCompleted(completed);
        }
    }

    /**
     * Shows the Time Picker Dialog and sets the dateTime
     */
    public void showTimePickerDialog() {
        TimePickerFragment timeFrag = new TimePickerFragment();
        timeFrag.setOnTimeSelectedListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                dateTime.set(Calendar.HOUR, hourOfDay);
                dateTime.set(Calendar.MINUTE, minute);
                tvDateAndTime.setText(android.text.format.DateFormat.format("MM/dd/yyyy h:mm a", dateTime.getTimeInMillis()));
            }
        });
        timeFrag.show(getSupportFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment {

        TimePickerDialog.OnTimeSetListener mListener;

        public TimePickerFragment() {
            // Default constructor. Required
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), mListener, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
        }

        public void setOnTimeSelectedListener(TimePickerDialog.OnTimeSetListener listener) {
            mListener = listener;
        }

    }

    /**
     * Shows the Date Picker Dialog and sets the dateTime
     */
    public void showDatePickerDialog() {
        DatePickerFragment dateFrag = new DatePickerFragment();
        dateFrag.setOnDateSelectedListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                dateTime.set(Calendar.YEAR, year);
                dateTime.set(Calendar.MONTH, month);
                dateTime.set(Calendar.DATE, day);
                tvDateAndTime.setText(android.text.format.DateFormat.format("MM/dd/yyyy h:mm a", dateTime.getTimeInMillis()));
            }
        });
        dateFrag.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment {

        DatePickerDialog.OnDateSetListener mListener;

        public DatePickerFragment() {
            // Default constructor. Required
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), mListener, year, month, day);
        }

        public void setOnDateSelectedListener(DatePickerDialog.OnDateSetListener listener) {
            mListener = listener;
        }

    }
}

