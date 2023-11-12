package com.example.myapplication;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Broadcasts.AlertReceiver;
import com.example.myapplication.Database.AppDatabase;
import com.example.myapplication.Entity.Vacation;

import java.text.ParseException;
import java.util.Calendar;
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

        editTextStartDate.setOnClickListener(v -> showDatePickerDialog(editTextStartDate));
        editTextEndDate.setOnClickListener(v -> showDatePickerDialog(editTextEndDate));

        Button saveButton = findViewById(R.id.buttonSaveVacation);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveVacation();
            }
        });

        Button viewVacationsButton = findViewById(R.id.buttonViewVacations);
        viewVacationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddVacationActivity.this, VacationDetailActivity.class);
                startActivity(intent);
            }
        });

        Button setAlertButton = findViewById(R.id.buttonSetAlert);
        setAlertButton.setOnClickListener(v -> {
            if (currentVacation != null) {
                setAlarmForVacation(currentVacation, true);
                Toast.makeText(AddVacationActivity.this, "Alerts for vacation start and end have been set.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AddVacationActivity.this, "No vacation selected to set alert for.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddVacationActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    String SelectedDate = String.format(Locale.US, "%02d/%02d/%04d", month1 + 1, dayOfMonth, year1);
                    editText.setText(SelectedDate);
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
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
                    Vacation insertedVacation = db.vacationDao().getVacationById((int) vacationId);
                    runOnUiThread(() -> {
                        Toast.makeText(AddVacationActivity.this, "Vacation saved successfully", Toast.LENGTH_SHORT).show();
                        currentVacation = insertedVacation;

                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(AddVacationActivity.this, "Error saving vacation", Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            Toast.makeText(AddVacationActivity.this, "Please fill out all fields correctly. Ensure end date is after start date.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetFields() {
        editTextTitle.setText("");
        editTextHotel.setText("");
        editTextStartDate.setText("");
        editTextEndDate.setText("");

    }

    private void onSetAlertClicked(Vacation vacation, boolean isStarting) {
        if (vacation != null) {
            setAlarmForVacation(vacation, isStarting);
        }
    }

    private void setAlarmForVacation(Vacation vacation, boolean isStarting) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Log.d("SetAlarm", "Permission to schedule exact alarms not granted. Requesting permission.");
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            } else {
                Log.d("SetAlarm", "Permission to schedule exact alarms granted or not needed.");
            }
        }
        setAlarm(vacation, true);
        setAlarm(vacation, false);

    }

    private void setAlarm(Vacation vacation, boolean isStarting) {
        Log.d("SetAlarm", "Setting alarm for " + (isStarting ? "start" : "end") + " time.");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("VACATION_TITLE", vacation.getTitle());
        intent.putExtra("STARTING", isStarting);

        long timeInMillis = isStarting ? getDateMillis(vacation.getStartDate()) : getDateMillis(vacation.getEndDate());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (vacation.getId() * 2) + (isStarting ? 0 : 1), intent, PendingIntent.FLAG_IMMUTABLE);

        if (timeInMillis > System.currentTimeMillis()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    private long getDateMillis(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
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

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
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