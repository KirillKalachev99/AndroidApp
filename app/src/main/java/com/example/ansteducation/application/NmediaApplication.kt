package com.example.ansteducation.application

import android.app.Application
import com.example.ansteducation.auth.AppAuth

class NmediaApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppAuth.initApp(this)
    }
}