package com.example.testvk


import com.example.testvk.model.AudioFile
import com.example.testvk.model.Database

class Repo {

    private val audioDao = Database.instance.audioDao()

    suspend fun getAllAudioFiles(): List<AudioFile> {
        return audioDao.getAllAudioFiles()
    }

    suspend fun saveAudioFile(audioFile: AudioFile) {
        audioDao.insertAudioFile(audioFile)
    }

    suspend fun updateAudioFile(audioFile: AudioFile) {
        audioDao.updateAudioFile(audioFile)
    }

    suspend fun updatePlaying(audioFileNew: AudioFile?, audioFileOld: AudioFile?) {
        if (audioFileOld != null) updateAudioFile(audioFileOld.copy(isPlaying = false))
        if (audioFileNew != null && audioFileNew != audioFileOld) {
            updateAudioFile(audioFileNew.copy(isPlaying = true))
        }
    }
}