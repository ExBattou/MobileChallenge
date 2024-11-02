package com.crombie.mobilechallengeuala

import android.app.Application
import android.util.Log
import androidx.activity.result.launch
import androidx.compose.runtime.Applier
import com.crombie.mobilechallengeuala.ui.theme.Model.City
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MobileChallengeApp: Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseProvider.initialize(this)
        CoroutineScope(Dispatchers.IO).launch {
            prepopulateDatabase()
        }
    }

    private suspend fun prepopulateDatabase() {
        val cities = fetchCitiesFromGist("https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/0996accf70cb0ca0e16f9a99e0ee185fafca7af1/cities.json")
        DatabaseProvider.database.cityDao().insertAll(cities)
    }

    suspend fun fetchCitiesFromGist(urlString: String): List<City> = withContext(Dispatchers.IO) {
        try {
            val response = URL(urlString).readText()
            val gson = Gson()
            gson.fromJson(response, Array<City>::class.java).toList()
        } catch (e: Exception) {
            Log.e("FetchCities", "Error fetching or parsing cities", e)
            emptyList()
        }
    }
}