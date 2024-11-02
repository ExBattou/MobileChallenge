package com.crombie.mobilechallengeuala

import android.content.Context
import androidx.room.Room
import com.crombie.mobilechallengeuala.ui.theme.db.AppDatabase

object DatabaseProvider {
    lateinit var database: AppDatabase

    fun initialize(context: Context) {
        database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()
    }
}
