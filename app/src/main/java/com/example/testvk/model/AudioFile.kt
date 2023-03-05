package com.example.testvk.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration

@Entity(
    tableName = AudioFileContract.TABLE_NAME
)
data class AudioFile(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = AudioFileContract.Columns.ID)
    val id: Long = 0,
    @ColumnInfo(name = AudioFileContract.Columns.TITLE)
    val title: String,
    @ColumnInfo(name = AudioFileContract.Columns.DATE)
    val date: String,
    @ColumnInfo(name = AudioFileContract.Columns.ISPLAYING)
    val isPlaying: Boolean = false,
    @ColumnInfo(name = AudioFileContract.Columns.ISSAVED)
    val isSaved: Boolean = false,
)