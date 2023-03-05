package com.example.testvk

import android.app.Application
import com.example.testvk.model.Database

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Database.init(this)
    }
}