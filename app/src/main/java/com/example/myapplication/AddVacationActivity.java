package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Broadcasts.AlertReceiver;
import com.example.myapplication.Database.AppDatabase;
import com.example.myapplication.Entity.Vacation;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddVacationActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextHotel, editTextStartDate, editTextEndDate;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private Vacation currentVacation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vacation);

        editTextTitle = findViewById(R.id.editTextVacationTitle);
        editTextHotel = findViewById(R.id.editTextHotel);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);

        Button saveButton = findViewById(R.id.buttonSaveVacation);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveVacation();
            }
        });

        Button startAlertButton = findViewById(R.id.buttonSetStartAlert);
        startAlertButton.setOnClickListener(v -> onSetAlertClicked(currentVacation, true));

        Button endAlertButton = findViewById(R.id.buttonSetEndAlert);
        endAlertButton.setOnClickListener(v -> onSetAlertClicked(currentVacation, false));

        Button viewVacationsButton = findViewById(R.id.buttonViewVacations);
        viewVacationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddVacationActivity.this, VacationDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    private void saveVacation() {
        final String title = editTextTitle.getText().toString();
        final String hotel = editTextHotel.getText().toString();
        final String startDate = editTextStartDate.getText().toString();
        final String endDate = editTextEndDate.getText().toString();

        if (validateInput(title, hotel, startDate, endDate)) {
            Vacation vacation = new Vacation(0, title, hotel, startDate, endDate);
            executor.execute(() -> {
               try {
                   AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                   long vacationId = db.vacationDao().insert(vacation);
                   runOnUiThread(() -> {
                       Toast.makeText(AddVacationActivity.this, "Vacation saved successfully", Toast.LENGTH_SHORT).show();
                       currentVacation = new Vacation((int) vacationId, title, hotel, startDate, endDate);

                   });
               } catch (Exception e) {
                   runOnUiThread(() -> Toast.makeText(AddVacationActivity.this, "Error saving vacation", Toast.LENGTH_SHORT).show());
               }
            });
        } else {
            Toast.makeText(AddVacationActivity.this, "Please fill out all fields correctly. Ensure end date is after start date.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSetAlertClicked(Vacation vacation, boolean isStarting) {
        if (vacation != null) {
            setAlarmForVacation(vacation, isStarting);
        }
    }

    private void setAlarmForVacation(Vacation vacation, boolean isStarting) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);

        intent.putExtra("VACATION_TITLE", vacation.getTitle());

        // Set Start Date Alarm
        PendingIntent startIntent = PendingIntent.getBroadcast(this, vacation.getId(), intent, PendingIntent.FLAG_IMMUTABLE);
        long startMillis = getDateMillis(vacation.getStartDate());
        alarmManager.set(AlarmManager.RTC_WAKEUP, startMillis, startIntent);

        // Set End Date Alarm
        PendingIntent endIntent = PendingIntent.getBroadcast(this, vacation.getId() + 1, intent, 0);
        long endMillis = getDateMillis(vacation.getEndDate());
        alarmManager.set(AlarmManager.RTC_WAKEUP, endMillis, endIntent);
    }

    private long getDateMillis(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean validateInput(String title, String hotel, String startDateStr, String endDateStr) {
        if (title.isEmpty() || hotel.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        try {
            Date start = sdf.parse(startDateStr);
            Date end = sdf.parse(endDateStr);

            if (start == null || end == null) {
                return false;
            }
            return !start.after(end);
        } catch (ParseException e) {
            return false;
        }
    }
}