package com.lolha.learningapp

import android.app.Application
import com.lolha.learningapp.data.local.LearningDatabase

class LearningApp : Application() {
    val database: LearningDatabase by lazy { LearningDatabase.create(this) }
}
