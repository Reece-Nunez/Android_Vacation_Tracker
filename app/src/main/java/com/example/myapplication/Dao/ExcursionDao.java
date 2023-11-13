package com.example.myapplication.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.Entity.Excursion;

import java.util.List;

@Dao
public interface ExcursionDao {
    @Query("SELECT * FROM excursions WHERE vacationId = :vacationId")
    LiveData<List<Excursion>> getExcursionsForVacation(int vacationId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Excursion excursion);

    @Update
    void update(Excursion excursion);

    @Delete
    void delete(Excursion excursion);

    @Query("SELECT * FROM excursions WHERE vacationId = :vacationId")
    List<Excursion> getExcursionsForVacationSync(int vacationId);

    @Query("SELECT * FROM excursions WHERE id = :excursionId")
    LiveData<Excursion> getExcursionById(int excursionId);

    @Query("SELECT * FROM excursions WHERE id = :excursionId")
    Excursion getExcursionByIdSync(int excursionId);
}
