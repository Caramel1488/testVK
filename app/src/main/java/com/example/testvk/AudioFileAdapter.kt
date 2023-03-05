package com.example.testvk


import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testvk.databinding.ItemAudioBinding
import com.example.testvk.model.AudioFile
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class AudioFileAdapter(
    private val onClick: (AudioFile) -> Unit
) : ListAdapter<AudioFile, AudioFileAdapter.Holder>(AudioFileDiffUtilCallback()) {

    private var mediaPlayer: MediaPlayer? = null

    class AudioFileDiffUtilCallback : DiffUtil.ItemCallback<AudioFile>() {
        override fun areItemsTheSame(oldItem: AudioFile, newItem: AudioFile): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AudioFile, newItem: AudioFile): Boolean {
            return oldItem == newItem
        }
    }

    inner class Holder(
        private val binding: ItemAudioBinding,
        onClick: (AudioFile) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var current: AudioFile? = null

        private var timer: CountDownTimer? = null


        init {
            binding.playButton.setOnClickListener {
                current?.let(onClick)

            }
            binding.pauseButton.setOnClickListener {
                current?.let(onClick)
                timer?.cancel()
            }
        }

        fun bind(audioFile: AudioFile) {
            current = audioFile
            binding.audioTitle.text = audioFile.title
            binding.audioDate.text = audioFile.date


            if (!audioFile.isPlaying) {
                with(binding) {
                    timer?.cancel()
                    timer = null
                    playButton.visibility = View.VISIBLE
                    pauseButton.visibility = View.GONE
                    progressDurationBar.visibility = View.GONE
                    playingDuration.visibility = View.GONE
                    duration.visibility = View.GONE
                }
            } else {
                with(binding) {
                    val durationMillis = mediaPlayer!!.duration
                    val currentMillis = mediaPlayer!!.currentPosition

                    playButton.visibility = View.GONE
                    pauseButton.visibility = View.VISIBLE
                    progressDurationBar.visibility = View.VISIBLE
                    playingDuration.visibility = View.VISIBLE
                    playingDuration.text = currentMillis.toString()
                    duration.visibility = View.VISIBLE
                    duration.text = "${durationMillis / 1000 / 60}:${durationMillis / 1000 % 60}"
                    progressDurationBar.max = durationMillis

                    makeTimer(binding, mediaPlayer!!.duration)
                    timer?.start()
                }
            }
        }

        private fun makeTimer(binding: ItemAudioBinding, durationMillis: Int){
            timer = object : CountDownTimer(durationMillis.toLong(), 1000) {
                override fun onTick(p0: Long) {
                    val now = durationMillis - p0
                    val minutes = (now / 1000 / 60)
                    val seconds = (now / 1000 % 60)
                    binding.playingDuration.text = "${minutes}:${seconds}"
                    binding.progressDurationBar.progress = now.toInt()
                }

                override fun onFinish() {
                    binding.progressDurationBar.progress = durationMillis
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding, onClick)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getMediaPlayer(newMediaPlayer: MediaPlayer?) {
        mediaPlayer = newMediaPlayer
    }

    fun clearMediaPlayer() {
        mediaPlayer!!.release()
        mediaPlayer = null
    }
}