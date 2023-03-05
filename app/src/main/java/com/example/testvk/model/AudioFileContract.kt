package com.example.testvk.model

object AudioFileContract {

    const val TABLE_NAME = "audioFile"

    object Columns{
        const val ID = "audioID"
        const val TITLE = "audioTitle"
        const val DATE = "audioDate"
        const val DURATION = "duration"
        const val ISSAVED = "isSaved"
        const val ISPLAYING = "isPlaying"
    }
}