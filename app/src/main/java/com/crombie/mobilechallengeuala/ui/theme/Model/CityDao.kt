package com.crombie.mobilechallengeuala.ui.theme.Model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CityDao {
    @Query("SELECT * FROM city")
    fun getAll(): LiveData<List<City>>

    @Insert
    fun insertAll(cities: List<City>)

    @Delete
    fun delete(city: City)
}