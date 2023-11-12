package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Database.AppDatabase;
import com.example.myapplication.Entity.Vacation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VacationDetailActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextHotel, editTextStartDate, editTextEndDate;
    private ExecutorService executorService;
    private int vacationId;
    private Vacation currentVacation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_detail);

        editTextTitle = findViewById(R.id.editTextVacationTitle);
        editTextHotel = findViewById(R.id.editTextHotel);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        Button saveUpdateButton = findViewById(R.id.buttonSaveUpdate);
        Button deleteButton = findViewById(R.id.buttonDelete);

        vacationId = getIntent().getIntExtra("VACATION_ID", -1);
        executorService = Executors.newSingleThreadExecutor();

        loadVacationData();
        setupButtonListeners();

        saveUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUpdateVacation();
            }
        });
    }

    private void saveUpdateVacation() {
        final String title = editTextTitle.getText().toString();
        final String hotel = editTextHotel.getText().toString();
        final String startDate = editTextStartDate.getText().toString();
        final String endDate = editTextEndDate.getText().toString();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

                if (currentVacation == null) {
                    Vacation newVacation = new Vacation(0, title, hotel, startDate, endDate);
                    db.vacationDao().insert(newVacation);
                } else {
                    currentVacation.setTitle(title);
                    currentVacation.setHotel(hotel);
                    currentVacation.setStartDate(startDate);
                    currentVacation.setEndDate(endDate);
                    db.vacationDao().update(currentVacation);
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VacationDetailActivity.this, "Vacation saved successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }

    private void loadVacationData() {
        executorService.execute(() -> {

            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            Vacation vacation = db.vacationDao().getVacationById(vacationId);

            new Handler(Looper.getMainLooper()).post(() -> {
                    if (vacation != null) {
                        currentVacation = vacation;
                        editTextTitle.setText(vacation.getTitle());
                        editTextHotel.setText(vacation.getHotel());
                        editTextStartDate.setText(vacation.getStartDate().toString());
                        editTextEndDate.setText(vacation.getEndDate().toString());
                    } else {
                        editTextTitle.setText("");
                        editTextHotel.setText("");
                        editTextStartDate.setText("");
                        editTextEndDate.setText("");
                    }
                });
            });
        }

        private void setupButtonListeners() {
            Button deleteButton = findViewById(R.id.buttonDelete);
            deleteButton.setOnClickListener(v -> deleteVacation());
            }

        private void deleteVacation() {
            if (currentVacation != null) {
                executorService.execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    db.vacationDao().delete(currentVacation);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(VacationDetailActivity.this, "Vacation deleted successfully!", Toast.LENGTH_SHORT).show();
                });
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
