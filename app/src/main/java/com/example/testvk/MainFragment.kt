package com.example.testvk

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.testvk.databinding.MainFragmentBinding
import com.example.testvk.model.AudioFile
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import ru.skillbox.contentprovider.utils.autoCleared
import timber.log.Timber
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainFragment : Fragment(R.layout.main_fragment) {

    private lateinit var authLauncher: ActivityResultLauncher<Collection<VKScope>>
    private val viewModel by viewModels<VM>()
    private var audioFileAdapter: AudioFileAdapter by autoCleared()
    private val binding: MainFragmentBinding by viewBinding(MainFragmentBinding::bind)
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        authLauncher = VK.login(requireActivity()) { result ->
            when (result) {
                is VKAuthenticationResult.Success -> {
                    Toast.makeText(requireContext(), "success auth", Toast.LENGTH_SHORT).show()
                    initList()
                    observe()
                }
                is VKAuthenticationResult.Failed -> {
                    showLoginDialog()
                }
            }
        }

        showLoginDialog()

        binding.buttonStartRecording.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(requireActivity(), permissions, 0)
            } else {
                if (state) {
                    Toast.makeText(requireContext(), "Oooops", Toast.LENGTH_SHORT).show()
                } else {
                    showNameDialog()
                }
            }
        }

        binding.buttonStopRecording.setOnClickListener {
            stopRecording()
        }

    }

    private fun startRecording() {
        try {
            binding.buttonStartRecording.visibility = View.GONE
            binding.buttonStopRecording.visibility = View.VISIBLE
            mediaRecorder?.setOutputFile(output)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(requireContext(), "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (state) {
            binding.buttonStartRecording.visibility = View.VISIBLE
            binding.buttonStopRecording.visibility = View.GONE
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            viewModel.saveAudioFile(
                AudioFile(
                    title = output?.toUri()?.lastPathSegment.toString(),
                    date = LocalDateTime.now().format(formatter),
                )
            )
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            output = null
            state = false
        } else {
            Toast.makeText(requireContext(), "You are not recording right now!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun showLoginDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Login")
            .setMessage("To use this app log in VK")
            .setPositiveButton("Lets go") { _, _ ->
                authLauncher.launch(arrayListOf(VKScope.WALL, VKScope.PHOTOS))
            }
            .setNegativeButton("Nah") { _, _ ->
                requireActivity().finish()
            }
            .show()
    }

    private fun showNameDialog() {
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Name ur audio file")

        val input = EditText(requireContext())
        input.hint = "Enter Name"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Lets go") { _, _ ->
            try {
                val fileName = input.text.toString()
                output =
                    Environment.getExternalStorageDirectory().absolutePath + "/${fileName}"
                startRecording()
            } catch (t: Throwable) {
                Timber.e(t)
            }
        }
            .show()
    }

    private fun initList() {
        audioFileAdapter = AudioFileAdapter {
            if (!it.isPlaying) {
                playAudioUri((Environment.getExternalStorageDirectory().absolutePath + "/" + it.title).toUri())
                viewModel.updatePlaying(it)
                audioFileAdapter.getMediaPlayer(mediaPlayer)
            } else {
                stopAudio()
                viewModel.updatePlaying(null)
                audioFileAdapter.clearMediaPlayer()
            }
        }
        with(binding.audioList) {
            adapter = audioFileAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observe() {
        viewModel.getAllAudioFiles()
        viewModel.audioFileList.observe(viewLifecycleOwner) {
            audioFileAdapter.submitList(it)
        }
    }

    private fun playAudioUri(uri: Uri) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(requireActivity(), uri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            prepare()
            start()
        }
        mediaPlayer?.setOnCompletionListener {
            viewModel.updatePlaying(null)
        }
    }

    private fun stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    override fun onDetach() {
        super.onDetach()
          if (mediaRecorder != null) {
            mediaRecorder!!.release()
            mediaRecorder = null
        }
        viewModel.updatePlaying(null)
        audioFileAdapter.getMediaPlayer(null)
    }
}