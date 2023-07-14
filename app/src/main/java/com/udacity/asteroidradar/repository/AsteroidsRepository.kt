package com.udacity.asteroidradar.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.AsteroidApi.radar
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDatabaseModel
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AsteroidsRepository(private val database: AsteroidsDatabase) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val now = (LocalDateTime.now()).format(DateTimeFormatter.ISO_DATE)

    @RequiresApi(Build.VERSION_CODES.O)
    private val pastWeek = (LocalDateTime.now().minusDays(7)).format(DateTimeFormatter.ISO_DATE)

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroids()) {
            it.asDomainModel()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    val today:LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getToday(now)){
            it.asDomainModel()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    val week:LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getWeek(pastWeek,now)){
            it.asDomainModel()
        }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val networkJson = radar.getAsteroidsAsync().await()
                val list = parseAsteroidsJsonResult(JSONObject(networkJson))
                database.asteroidDao.insertAll(*list.asDatabaseModel())
            } catch (e: Exception) {
                Timber.e(e.message)
            }
        }
    }
}