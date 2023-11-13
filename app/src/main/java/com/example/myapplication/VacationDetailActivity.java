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
    private Vacation currentVacation;

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

    public void onShareClicked(Vacation vacation) {
        shareVacationDetails(vacation);
    }

    private void shareVacationDetails(Vacation vacation) {
        String shareText = "Vacation Details:\n" +
                "Title: " + vacation.getTitle() + "\n" +
                "Hotel: " + vacation.getHotel() + "\n" +
                "Start Date: " + vacation.getStartDate() + "\n" +
                "End Date: " + vacation.getEndDate();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
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
                })
               .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
               .create();
        alertDialog.show();
    }

    private void editVacation(Vacation vacation) {
       Intent intent = new Intent(VacationDetailActivity.this, AddVacationActivity.class);
       intent.putExtra("vacationId", vacation.getId());
       startActivity(intent);
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
