package com.example.testvk.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AudioFile::class], version = AudioDatabase.DB_VERSION
)
abstract class AudioDatabase: RoomDatabase() {

    abstract fun audioDao(): AudioDao

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "database"
    }
}