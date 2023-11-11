package com.example.myapplication.Dao;

import androidx.room.Dao;
import androidx.room.Insert;

import com.example.myapplication.Entity.Vacation;

@Dao
public interface VacationDao {

    @Insert
    void insert(Vacation vacation);

    // TODO: Add other methods
}
