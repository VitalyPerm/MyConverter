package com.example.myconverter

import android.content.ContentProvider
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.example.myconverter.ui.theme.MyConverterTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.resume

const val TAG = "check___"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            MyConverterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {

                        Button(
                            onClick = ::convert,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(text = "Convert!")
                        }
                    }
                }
            }
        }
    }


    private fun convert() {
        lifecycleScope.launch {

            val ktgWavUri = Uri.fromFile(File(cacheDir, "ktg.wav"))

            withContext(Dispatchers.IO) {
                try {
                    val outFile = File(cacheDir, "output.mp3")

                    val uri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "com.example.myconverter.file_provider",
                        outFile
                    )

                    val result = convertAudioFile(
                        input = ktgWavUri,
                        output = uri
                    )
                    if (result.not()) return@withContext

                    val player = MediaPlayer()
                    val fis = FileInputStream(outFile)
                    player.setDataSource(fis.fd)
                    player.prepare()
                    player.start()
                } catch (e: Exception) {
                    Log.d("check___", "error: ${e.message} ")
                }

            }
        }

    }

    private suspend fun convertAudioFile(
        input: Uri,
        output: Uri,
    ) = suspendCancellableCoroutine { cont ->
        val inputPath = FFmpegKitConfig.getSafParameterForRead(this, input)
        val outputPath = FFmpegKitConfig.getSafParameterForWrite(this, output)
        val session = FFmpegKit.executeAsync(
            "-i $inputPath -acodec libmp3lame -b:a 256k $outputPath", { session ->
                val returnCode = session.returnCode
                val success = returnCode.isValueSuccess
                cont.resume(success)
            },
            { log -> },
            { statistics -> }
        )
        cont.invokeOnCancellation { session.cancel() }
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
    MyConverterTheme {
        Greeting("Android")
    }
}