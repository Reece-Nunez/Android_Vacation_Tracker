package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.VacationAdapter;
import com.example.myapplication.Database.AppDatabase;
import com.example.myapplication.Entity.Vacation;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VacationDetailActivity extends AppCompatActivity implements VacationAdapter.OnVacationListener {
    private RecyclerView recyclerView;
    private VacationAdapter adapter;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_detail);

        recyclerView = findViewById(R.id.recyclerViewVacations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VacationAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadVacations();
    }

    private void loadVacations() {
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        db.vacationDao().getAll().observe(this, vacations -> {
            adapter.setVacations(vacations);
        });
    }

    @Override
    public void onEditClicked(Vacation vacation) {
        // Implement logic to handle vacation edit
        // This might include opening a new activity or a dialog with vacation details
        AlertDialog alertDialog = new AlertDialog.Builder(this)
               .setTitle("Edit Vacation")
               .setMessage("Are you sure you want to edit this vacation?")
               .setPositiveButton("Yes", (dialog, which) -> {
                    editVacation(vacation);
                    dialog.dismiss();
                })
               .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
               .create();
        alertDialog.show();
    }

    private void editVacation(Vacation vacation) {
        executorService.execute(() -> {
            AppDatabase appDatabase = AppDatabase.getDatabase(VacationDetailActivity.this);
            appDatabase.vacationDao().update(vacation);
            runOnUiThread(this::loadVacations);
        });
        finish();
    }

    @Override
    public void onDeleteClicked(Vacation vacation) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Delete Vacation")
                .setMessage("Are you sure you want to delete this vacation?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteVacation(vacation);
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create();
        alertDialog.show();
    }

    private void deleteVacation(Vacation vacation) {
        executorService.execute(() -> {
            AppDatabase appDatabase = AppDatabase.getDatabase(VacationDetailActivity.this);
            appDatabase.vacationDao().delete(vacation);
            runOnUiThread(this::loadVacations);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}
