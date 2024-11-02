package com.crombie.mobilechallengeuala.ui.theme.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.crombie.mobilechallengeuala.ui.theme.Model.City
import com.crombie.mobilechallengeuala.ui.theme.Model.CityDao

@Database(entities = [City::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
}