package org.hackertube.android

import android.app.Application
import org.hackertube.android.data.local.UserPreferences
import org.hackertube.android.data.repository.VideoRepository

class HackerTubeApp : Application() {
    lateinit var videoRepository: VideoRepository
        private set
    lateinit var userPreferences: UserPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        videoRepository = VideoRepository(this)
        userPreferences = UserPreferences(this)
    }
}
