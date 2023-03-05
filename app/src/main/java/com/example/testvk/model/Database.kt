package com.example.testvk.model

import android.content.Context
import androidx.room.Room

object Database {
    lateinit var instance: AudioDatabase
        private set

    fun init(context: Context){
        instance = Room.databaseBuilder(
            context,
            AudioDatabase::class.java,
            AudioDatabase.DB_NAME
        ).build()
    }
}