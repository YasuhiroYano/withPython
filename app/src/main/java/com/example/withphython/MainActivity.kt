package com.example.withphython

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.example.withphython.ui.theme.WithPhythonTheme

import android.Manifest
import com.chaquo.python.android.AndroidPlatform
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.layout.height
import androidx.glance.layout.width
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var isAudioPermissionGranted = false
    private var _isRecording = false
    private val isRecording: Boolean
        get() = _isRecording
    private val _audioDataList = mutableStateListOf<Float>()
    val audioDataList: SnapshotStateList<Float> = _audioDataList

    // Activity Result API を使用してパーミッションリクエストの結果を受け取る
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // パーミッションが許可された場合の処理
                isAudioPermissionGranted = true
                startRecordingIfPermissionGranted()
            } else {
                // パーミッションが拒否された場合の処理
                isAudioPermissionGranted = false
                Toast.makeText(this, "マイクのパーミッションが拒否されました", Toast.LENGTH_SHORT).show()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val py = Python.getInstance()
        val module = py.getModule("hello")
        val txt1 = module.callAttr("hello_world")
        val txt2 = module.callAttr("set_text", "Good Morning")
        println(txt1)
        println(txt2)
        // マイクのパーミッションをチェック
        checkAudioPermission()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        setContent {
            WithPhythonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = txt1.toString(),
                        modifier = Modifier.padding(innerPadding)
                    )
                    AudioWaveformGraph(audioDataList)
                }
            }
        }
    }
    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // パーミッションが許可されていない場合はリクエスト
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            // パーミッションが許可されている場合の処理
            isAudioPermissionGranted = true
        }
    }

    private fun startRecordingIfPermissionGranted() {
        if (isAudioPermissionGranted&& !isRecording) {
            _isRecording = true
            val py = Python.getInstance()
            CoroutineScope(Dispatchers.IO).launch {
                startRecording(py, this@MainActivity) {
                    _isRecording
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _isRecording = false
    }
    fun addAudioData(data: Float) {
        _audioDataList.add(data)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WithPhythonTheme {
        Greeting("Android")
    }
}
@Composable
fun AudioWaveformGraph(audioDataList: List<Float>) {
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2

        val dataPoints = audioDataList.size
        if (dataPoints > 1) {
            val step = canvasWidth / (dataPoints - 1)
            for (i in 0 until dataPoints - 1) {
                val startX = i * step
                val startY = centerY + audioDataList[i] * centerY
                val endX = (i + 1) * step
                val endY = centerY + audioDataList[i + 1] * centerY
                drawLine(
                    color = Color.Green,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}