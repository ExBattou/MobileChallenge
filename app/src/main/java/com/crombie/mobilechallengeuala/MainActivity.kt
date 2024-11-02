package com.crombie.mobilechallengeuala

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.crombie.mobilechallengeuala.ui.theme.MobileChallengeUALATheme
import com.crombie.mobilechallengeuala.ui.theme.Model.City
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL


val BASE_URL =
    "https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/0996accf70cb0ca0e16f9a99e0ee185fafca7af1/cities.json"


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileChallengeUALATheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(navController)
                }
            }
        }
    }


}

@Composable
fun AppNavHost(navHostController: NavHostController) {
    NavHost(navController = navHostController, startDestination = "home") {
        composable("home") { CityListView(navHostController) }
        composable(route = "map/{lat}/{long}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("long") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val long = backStackEntry.arguments?.getString("long")?.toDoubleOrNull() ?: 0.0

            val configuration = LocalConfiguration.current
            when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    Row {
                        CityListView(navHostController)
                        GoogleMap(lat, long)
                    }
                }

                Configuration.ORIENTATION_PORTRAIT -> {
                    Column {
                        BackButton { navHostController.popBackStack() }
                        GoogleMap(lat, long)
                    }
                }
            }
        }
    }
}


@Composable
fun CityListView(navHostController: NavHostController) {
    val cityList = remember { mutableStateListOf<City>() }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                if (cityList.isEmpty()) {
                    cityList.addAll(fetchCitiesFromGist("https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/0996accf70cb0ca0e16f9a99e0ee185fafca7af1/cities.json"))
                }
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center)
            {
                CircularProgressIndicator()
            }

        } else {
            CityList(cityList, navHostController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CityListPreview() {
    MobileChallengeUALATheme {
        val cityList = remember { mutableStateListOf<City>() }
        var isLoading by remember { mutableStateOf(true) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    cityList.addAll(
                        fetchCitiesFromGist(BASE_URL)
                    )
                } finally {
                    isLoading = false
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(160.dp))
        } else {
            CityList(cityList, navController = rememberNavController())
        }
    }
}

@Composable
fun CityList(cities: List<City>, navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    Column (modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()) {
        OutlinedTextField(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") }
        )
        val filteredCities = if (searchQuery.isNotEmpty()) {
            cities.filter { it.name.startsWith(searchQuery, ignoreCase = true) }
        } else {
            cities
        }
        LazyColumn (contentPadding = PaddingValues(vertical = 15.dp)){
            items(filteredCities) { city ->
                CityRow(city) { selectedCity ->
                    navController.navigate("map/${selectedCity.coord.lat}/${selectedCity.coord.lon}")
                }
            }
        }
    }

}

@Composable
fun CityRow(city: City, onClick: (City) -> Unit) {
    Row(modifier = Modifier.clickable { onClick(city) }) {
        Row() {
            Text(text = "${city.name}, ${city.country}")
        }
    }

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

suspend fun fetchCitiesFromGist(context: Context, urlString: String): List<City> =
    withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "cities.json") // Create a file in internal storage

        if (file.exists()) {
            // Read from local file if it exists
            val jsonString = file.readText()
            val gson = Gson()
            return@withContext gson.fromJson(jsonString, Array<City>::class.java).toList()
        } else {
            // Fetch from Gist and save to local file
            try {
                val response = URL(urlString).readText()
                file.writeText(response) // Save to local file
                val gson = Gson()
                return@withContext gson.fromJson(response, Array<City>::class.java).toList()
            } catch (e: Exception) {
                Log.e("FetchCities", "Error fetching or parsing cities", e)
                return@withContext emptyList()
            }
        }
    }

@Composable
fun GoogleMap(lat: Double = 39.934444, lng: Double = 43.600555) {
    val where = LatLng(lat, lng)
    val whereMarketState = rememberMarkerState(position = where)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(where, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = whereMarketState,
            title = "Singapore",
            snippet = "Marker in Singapore"
        )
    }
}


@Composable
fun BackButton(onBackClick: () -> Unit) {
    Row {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(text = "Back")
    }

}
