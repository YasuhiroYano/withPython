package com.example.withphython

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
import com.chaquo.python.Python
import com.example.withphython.ui.theme.WithPhythonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val py = Python.getInstance()
        val module = py.getModule("hello")
        val txt1 = module.callAttr("hello_world")
        val txt2 = module.callAttr("set_text", "Good Morning")
        println(txt1)
        println(txt2)
        setContent {
            WithPhythonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = txt2.toString(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
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