package com.example.myapplication.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.Entity.Vacation;

import java.util.List;

@Dao
public interface VacationDao {
    @Query("SELECT * FROM vacations")
    LiveData<List<Vacation>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Vacation vacation);

    @Update
    void update(Vacation vacation);

    @Delete
    void delete(Vacation vacation);

    @Query("SELECT * FROM vacations WHERE id = :vacationId")
    LiveData<Vacation> getVacationById(int vacationId);

    @Query("SELECT * FROM vacations")
    List<Vacation> getAllVacationsSync();

    @Query("SELECT * FROM vacations WHERE id = :vacationId")
    Vacation getVacationByIdSync(int vacationId);
}
