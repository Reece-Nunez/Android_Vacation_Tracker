package com.example.myapplication.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "excursions",
        foreignKeys = @ForeignKey(entity = Vacation.class,
                                  parentColumns = "id",
                                  childColumns = "vacationId",
                                  onDelete = ForeignKey.RESTRICT))
public class Excursion {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "vacationId")
    private int vacationId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getVacationId() {
        return vacationId;
    }

    public void setVacationId(int vacationId) {
        this.vacationId = vacationId;
    }

    @Override
    public String toString() {
        return "Excursion{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", vacationId=" + vacationId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass()!= o.getClass()) return false;

        Excursion excursion = (Excursion) o;

        if (id!= excursion.id) return false;
        if (vacationId!= excursion.vacationId) return false;
        if (!Objects.equals(title, excursion.title)) return false;
        return Objects.equals(date, excursion.date);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title!= null? title.hashCode() : 0);
        result = 31 * result + (date!= null? date.hashCode() : 0);
        result = 31 * result + vacationId;
        return result;
    }
}
