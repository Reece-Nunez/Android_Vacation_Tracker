package com.example.myapplication;

import android.app.DatePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Database.AppDatabase;
import com.example.myapplication.Entity.Excursion;
import com.example.myapplication.Entity.Vacation;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddEditExcursionActivity extends AppCompatActivity {
    private EditText editTextExcursionTitle, editTextExcursionDate;
    private Button buttonSaveExcursion;
    private Excursion currentExcursion;
    private int vacationId;
    private AppDatabase db;

    private Executor myExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_excursion);

        initializeViews();

        myExecutor = Executors.newSingleThreadExecutor();

        db = AppDatabase.getDatabase(getApplicationContext());
        vacationId = getIntent().getIntExtra("vacationId", -1);
        if (vacationId == -1) {
            Toast.makeText(this, "Vacation ID is missing", Toast.LENGTH_SHORT).show();
        }

        int excursionId = getIntent().getIntExtra("excursionId", -1);
        if (excursionId != -1) {
            observeExcursionDetails(excursionId);
        }

        buttonSaveExcursion.setOnClickListener(v -> {
            saveOrUpdateExcursion();

        });
    }

    private void initializeViews() {
        editTextExcursionTitle = findViewById(R.id.editTextExcursionTitle);
        editTextExcursionDate = findViewById(R.id.editTextExcursionDate);
        buttonSaveExcursion = findViewById(R.id.buttonSaveExcursion);

        editTextExcursionDate.setOnClickListener(v -> showDatePickerDialog(editTextExcursionDate));

    }

    private void observeExcursionDetails(int excursionId) {
        db.excursionDao().getExcursionById(excursionId).observe(this, excursion -> {
            if (excursion != null) {
                editTextExcursionTitle.setText(excursion.getTitle());
                editTextExcursionDate.setText(excursion.getDate());
                currentExcursion = excursion;
            }
        });
    }


    private void saveOrUpdateExcursion() {
        String title = editTextExcursionTitle.getText().toString();
        String date = editTextExcursionDate.getText().toString();

        Log.d("AddEditExcursionActivity", "Attempting to save or update excursion");

        // Validate the date format before proceeding
        if (!isValidDate(date)) {
            Toast.makeText(this, "Invalid date format.", Toast.LENGTH_SHORT).show();
            Log.e("AddEditExcursionActivity", "Invalid date format.");
            return;
        }

        db.vacationDao().getVacationById(vacationId).observe(this, vacation -> {
            if (vacation == null) {
                Toast.makeText(this, "Vacation not found.", Toast.LENGTH_SHORT).show();
                Log.e("AddEditExcursionActivity", "Vacation not found for ID: " + vacationId);
                return;
            }

            if (!isDateWithinRange(date, vacation)) {
                Toast.makeText(this, "Date is not within the vacation range.", Toast.LENGTH_SHORT).show();
                Log.e("AddEditExcursionActivity", "Date is not within the vacation range.");
                return;
            }

            persistExcursion(title, date);
        });
    }

    private void persistExcursion(String title, String date) {
        myExecutor.execute(() -> {
            Excursion excursionToSave = currentExcursion == null ? new Excursion() : currentExcursion;
            excursionToSave.setVacationId(vacationId);
            excursionToSave.setTitle(title);
            excursionToSave.setDate(date);

            try {
                if (currentExcursion == null) {
                    db.excursionDao().insert(excursionToSave);
                    Log.d("AddEditExcursionActivity", "Excursion saved successfully.");
                } else {
                    db.excursionDao().update(excursionToSave);
                    Log.d("AddEditExcursionActivity", "Excursion updated successfully.");
                }

                runOnUiThread(this::finish);
            } catch (Exception e) {
                Log.e("AddEditExcursionActivity", "Error saving excursion: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(AddEditExcursionActivity.this, "Error saving excursion: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private boolean isValidDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            dateFormat.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isDateWithinRange(String dateString, Vacation vacation) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date excursionDate = dateFormat.parse(dateString);
            Date vacationStart = dateFormat.parse(vacation.getStartDate());
            Date vacationEnd = dateFormat.parse(vacation.getEndDate());

            return excursionDate != null && !excursionDate.before(vacationStart) && !excursionDate.after(vacationEnd);
            } catch (ParseException e) {
                return false;
            }
        }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddEditExcursionActivity.this,
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
}
