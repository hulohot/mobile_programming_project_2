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

        // Extras from Intent
        Bundle extras = getIntent().getExtras();
        final ToDoItem toDoItem = (ToDoItem) extras.getSerializable("ToDoItem");
        Toast.makeText(getApplicationContext(),"ToDoItem is " + toDoItem.getTitle(), Toast.LENGTH_LONG).show();


        // Load values from DB if item exists already
        if(toDoItem != null) {
            chkBoxDone.setChecked(toDoItem.getCompleted());
            etItemTitle.setText(toDoItem.getTitle());
            etItemContent.setText(toDoItem.getContent());
            tvDateAndTime.setText(android.text.format.DateFormat.format("MM/dd/yyyy h:mm a", toDoItem.getDueDate()));
        } else {
            // No toDoItem exists
            updateToDoItem(toDoItem, "Default To-Do Title", "Default To-Do Body", dateTime.getTimeInMillis(), false);
            chkBoxDone.setChecked(false);
            etItemTitle.setText("Default To-Do Title");
            etItemContent.setText("Default To-Do Body");
            tvDateAndTime.setText("Set Date Time");
        }

        tvDateAndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(view);
                showTimePickerDialog(view);
                tvDateAndTime.setText(android.text.format.DateFormat.format("MM/dd/yyyy h:mm a", dateTime.getTimeInMillis()));
            }
        });

        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Set Alarm Service
                if(Build.VERSION.SDK_INT >= 23) {
                    almManager = view.getContext().getSystemService(AlarmManager.class);
                } else {
                    almManager = (AlarmManager) view.getContext().getSystemService(Context.ALARM_SERVICE);
                }

                // Wrap toDoItem in a bundle
                // https://stackoverflow.com/questions/40480355/pass-serializable-object-to-pending-intent/40515978#40515978
                Bundle bundle = new Bundle();
                updateToDoItem(toDoItem, etItemTitle.getText().toString(), etItemContent.getText().toString(), dateTime.getTimeInMillis(), chkBoxDone.isChecked());
                bundle.putSerializable("ToDoItem", toDoItem);

                // Create Intent
                Intent alarmNotificationIntent = new Intent(view.getContext(), AlarmNotification.class);
                alarmNotificationIntent.putExtra("bundle", bundle);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(view.getContext(), UUID.randomUUID().hashCode(), alarmNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                almManager.setExact(AlarmManager.RTC_WAKEUP, dateTime.getTimeInMillis(), alarmIntent);

                // Finish the activity
                Intent intent = new Intent();
                intent.putExtra("ToDoItem", toDoItem);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        chkBoxDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO figure out if this needs to do anything, I don't believe that it does
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
//
//        getMenuInflater().inflate(R.menu.);
//        return true;
//    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

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

    public void showTimePickerDialog(View v) {
        DialogFragment timeFrag = new TimePickerFragment();
        timeFrag.show(getSupportFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            dateTime.add(Calendar.HOUR, hourOfDay);
            dateTime.add(Calendar.MINUTE, minute);
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment dateFrag = new DatePickerFragment();
        dateFrag.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            dateTime.add(Calendar.YEAR, year);
            dateTime.add(Calendar.MONTH, month);
            dateTime.add(Calendar.DATE, day);

        }
    }
}

