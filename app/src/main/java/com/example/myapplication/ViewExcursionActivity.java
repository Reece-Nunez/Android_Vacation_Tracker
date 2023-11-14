package com.example.myapplication;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.ExcursionAdapter;
import com.example.myapplication.Broadcasts.AlertReceiver;
import com.example.myapplication.Database.AppDatabase;
import com.example.myapplication.Entity.Excursion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

        loadExcursions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExcursions();
    }

    private void loadExcursions() {
        int vacationId = getIntent().getIntExtra("vacationId", -1);
        if (vacationId == -1) {
            return;
        }
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
        intent.putExtra("vacationId", excursion.getVacationId());
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
            runOnUiThread(() -> loadExcursions());
        });
    }

    @Override
    public void onSetExcursionAlertClicked(Excursion excursion) {
        Calendar calendar = parseExcursionDate(excursion.getDate());
        Intent alertIntent = new Intent(this, AlertReceiver.class);
        alertIntent.putExtra("excursion_title", excursion.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, excursion.getId(), alertIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Alert set for: " + excursion.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error setting the alarm", Toast.LENGTH_SHORT).show();
        }
    }

    private Calendar parseExcursionDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse date", Toast.LENGTH_SHORT).show();
        }
        return calendar;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
