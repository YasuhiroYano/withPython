package com.example.withphython

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.PyObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

fun startRecording(python: Python, mainActivity: MainActivity, getIsRecording: () -> Boolean) {
    val sampleRate = 44100 // サンプリングレート
    val channelConfig = AudioFormat.CHANNEL_IN_MONO // モノラル
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT // 16bit PCM
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)

    val buffer = ByteArray(bufferSize)
    audioRecord.startRecording()
    try {
        while (getIsRecording()) {
            val bytesRead = audioRecord.read(buffer, 0, buffer.size)
            if (bytesRead > 0) {
                // バイト配列を Python に渡す
                val py = python
                val module = py.getModule("audio_process") // Python モジュール名
                val pyBuffer = PyObject.fromJava(buffer)
                module.callAttr("process_audio", pyBuffer)

                // バイト配列を Float に変換してグラフ表示用に渡す
                val shortBuffer = ShortArray(bytesRead / 2)
                ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortBuffer)
                val maxAmplitude = 32767.0f // 16bit PCM の最大振幅
                for (i in 0 until shortBuffer.size) {
                    val shortValue: Short = shortBuffer[i]
                    val intValue: Int = shortValue.toInt() // Convert Short to Int
                    val amplitude = abs(intValue) / maxAmplitude // Now abs() can be used
                    mainActivity.addAudioData(amplitude)
                }
            }
        }
    } finally {
        audioRecord.stop()
        audioRecord.release()
    }
}