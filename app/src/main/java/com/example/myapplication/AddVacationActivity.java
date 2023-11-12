package com.example.myapplication;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vacation);

        editTextTitle = findViewById(R.id.editTextVacationTitle);
        editTextHotel = findViewById(R.id.editTextHotel);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);

        Button savedButton = findViewById(R.id.buttonSaveVacation);
        savedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveVacation();
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
                   db.vacationDao().insert(vacation);
                   runOnUiThread(() -> Toast.makeText(AddVacationActivity.this, "Vacation saved successfully", Toast.LENGTH_SHORT).show());
               } catch (Exception e) {
                   runOnUiThread(() -> Toast.makeText(AddVacationActivity.this, "Error saving vacation", Toast.LENGTH_SHORT).show());
               }
            });
        } else {
            Toast.makeText(AddVacationActivity.this, "Please fill out all fields correctly. Ensure end date is after start date.", Toast.LENGTH_SHORT).show();
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