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
    void insert(Vacation vacation);

    @Update
    void update(Vacation vacation);

    @Delete
    void delete(Vacation vacation);
}
