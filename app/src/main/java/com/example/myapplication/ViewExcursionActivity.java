package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.ExcursionAdapter;
import com.example.myapplication.Database.AppDatabase;
import com.example.myapplication.Entity.Excursion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewExcursionActivity extends AppCompatActivity implements ExcursionAdapter.OnExcursionListener {
    private RecyclerView recyclerView;
    private ExcursionAdapter adapter;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_excursion);

        int vacationId = getIntent().getIntExtra("vacationId", -1);

        recyclerView = findViewById(R.id.recyclerViewExcursions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExcursionAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadExcursions(vacationId);
    }

    private void loadExcursions(int vacationId) {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<Excursion> excursions = db.excursionDao().getExcursionsForVacationSync(vacationId);
            runOnUiThread(() -> {
                adapter.setExcursions(excursions);
            });
        });
    }

    public void onEditExcursionClicked(Excursion excursion) {
        Intent intent = new Intent(this, AddEditExcursionActivity.class);
        intent.putExtra("excursionId", excursion.getId());
        startActivity(intent);
    }


    public void onSaveExcursionClicked(Excursion excursion) {
        Intent intent = new Intent(this, AddEditExcursionActivity.class);
        intent.putExtra("excursionId", excursion.getId());
        startActivity(intent);
    }

    public void onDeleteExcursionClicked(Excursion excursion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Excursion");
        builder.setMessage("Are you sure you want to delete this excursion?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteExcursion(excursion);
            dialog.dismiss();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void deleteExcursion(Excursion excursion) {
        AppDatabase db = AppDatabase.getDatabase(this);
        executorService.execute(() -> {
            db.excursionDao().delete(excursion);
            runOnUiThread(() -> loadExcursions(excursion.getVacationId()));
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
