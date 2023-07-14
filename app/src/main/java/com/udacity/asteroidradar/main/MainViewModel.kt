package com.udacity.asteroidradar.main

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.AsteroidFilters
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi.radar
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainViewModel(application: Application) : ViewModel() {
    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidsRepository(database)

    init {
        viewModelScope.launch {
            asteroidsRepository.refreshAsteroids()
            getDayPicture()
        }
    }

    private val _filter = MutableLiveData(AsteroidFilters.ALL)

    fun onChangeFilter(filter: AsteroidFilters) {
        _filter.value = filter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val list = Transformations.switchMap(_filter) {
        when (it!!) {
            AsteroidFilters.TODAY -> asteroidsRepository.today
            AsteroidFilters.WEEK -> asteroidsRepository.week
            else -> asteroidsRepository.asteroids
        }
    }

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    private suspend fun getDayPicture() {
        withContext(Dispatchers.IO) {
            try {
                _pictureOfDay.postValue(radar.getPictureOfTheDay())
            } catch (e: Exception) {
                Timber.e(e.message)
            }
        }
    }

    private val _navigateToAsteroidDetails = MutableLiveData<Asteroid>()
    val navigateToAsteroidDetails
        get() = _navigateToAsteroidDetails

    fun onAsteroidClicked(asteroid: Asteroid) {
        _navigateToAsteroidDetails.value = asteroid
    }

    fun onAsteroidNavigated() {
        _navigateToAsteroidDetails.value = null
    }


}