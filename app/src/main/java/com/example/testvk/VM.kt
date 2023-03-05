package com.example.testvk

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testvk.model.AudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class VM : ViewModel() {

    private val repository = Repo()
    private val audioFileListLiveData = MutableLiveData<List<AudioFile>>()
    private val audioFileLastPlayingLiveData = MutableLiveData<AudioFile?>()

    val audioFileList: LiveData<List<AudioFile>>
        get() = audioFileListLiveData
    val audioFileLastPlaying: LiveData<AudioFile?>
        get() = audioFileLastPlayingLiveData

    fun getAllAudioFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                audioFileListLiveData.postValue(repository.getAllAudioFiles())
            } catch (t: Throwable) {
                Timber.tag("VM").e(t.message.toString())
            }
        }
    }

    fun saveAudioFile(audioFile: AudioFile) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveAudioFile(audioFile)
            } catch (t: Throwable) {
                Timber.tag("VM").e(t.message.toString())
            } finally {
                getAllAudioFiles()
            }
        }
    }

    fun updateAudioFile(audioFile: AudioFile) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateAudioFile(audioFile)
            } catch (t: Throwable) {
                Timber.tag("VM").e(t.message.toString())
            } finally {
                getAllAudioFiles()
            }
        }
    }

    fun updatePlaying(audioFileNew: AudioFile?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updatePlaying(audioFileNew, audioFileLastPlaying.value)
                audioFileLastPlayingLiveData.postValue(audioFileNew)
            } catch (t: Throwable) {
                Timber.tag("VM").e(t.message.toString())
            } finally {
                getAllAudioFiles()
            }
        }
    }
}