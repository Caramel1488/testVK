package com.example.testvk

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.testvk.databinding.MainFragmentBinding
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import java.io.IOException

class MainFragment() : Fragment(R.layout.main_fragment) {

    private lateinit var authLauncher: ActivityResultLauncher<Collection<VKScope>>

    private val binding: MainFragmentBinding by viewBinding(MainFragmentBinding::bind)
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authLauncher = VK.login(requireActivity()) { result ->
            when (result) {
                is VKAuthenticationResult.Success -> {
                    Toast.makeText(requireContext(), "success auth", Toast.LENGTH_SHORT).show()
                }
                is VKAuthenticationResult.Failed -> {
                    showLoginDialog()
                }
            }
        }

        showLoginDialog()

        mediaRecorder = MediaRecorder()
        output = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"

        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)

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
                startRecording()
            }
        }

        binding.buttonStopRecording.setOnClickListener {
            stopRecording()
        }

        binding.buttonPauseRecording.setOnClickListener {
            pauseRecording()
        }
    }

    private fun startRecording() {
        try {
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

    @SuppressLint("RestrictedApi", "SetTextI18n")
    private fun pauseRecording() {
        if (state) {
            if (!recordingStopped) {
                Toast.makeText(requireContext(), "Stopped!", Toast.LENGTH_SHORT).show()
                mediaRecorder?.pause()
                recordingStopped = true
                binding.buttonPauseRecording.text = "Resume"
            } else {
                resumeRecording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    private fun resumeRecording() {
        Toast.makeText(requireContext(), "Resume!", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        binding.buttonPauseRecording.text = "Pause"
        recordingStopped = false
    }

    private fun stopRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
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
}