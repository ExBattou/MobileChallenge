package com.crombie.mobilechallengeuala.ui.theme.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class City (
    @ColumnInfo(name = "country") val country: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "id") val id: Int,
    @PrimaryKey @ColumnInfo(name = "lon") val lon: Double,
    @ColumnInfo(name = "lat") val lat: Double
)
