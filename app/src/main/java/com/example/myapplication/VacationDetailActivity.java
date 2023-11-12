package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.VacationAdapter;
import com.example.myapplication.Database.AppDatabase;
import com.example.myapplication.Entity.Vacation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VacationDetailActivity extends AppCompatActivity implements VacationAdapter.OnVacationListener {
    private RecyclerView recyclerView;
    private VacationAdapter adapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<Vacation> vacations = db.vacationDao().getAllVacationsSync();
            runOnUiThread(() -> {
                adapter.setVacations(vacations);
            });
        });
    }

    @Override
    public void onEditClicked(Vacation vacation) {
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

        Intent intent = new Intent(VacationDetailActivity.this, VacationDetailActivity.class);
        intent.putExtra("vacationId", vacation.getId());
        startActivity(intent);
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
