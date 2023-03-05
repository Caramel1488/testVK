package com.example.testvk.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AudioDao {

    @Query("SELECT * FROM ${AudioFileContract.TABLE_NAME}")
    suspend fun getAllAudioFiles(): List<AudioFile>

    @Update
    suspend fun updateAudioFile(audioFile: AudioFile)

    @Delete
    suspend fun deleteAudioFile(audioFile: AudioFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioFile(audioFile: AudioFile)
}