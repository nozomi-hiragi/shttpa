package com.nozomi.shttpa

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nozomi.shttpa.ui.theme.ShttpaTheme
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var webServer: WebServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.webServer = WebServer(applicationContext, 8080)

        setContent {
            ShttpaTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Home(webServer = this.webServer)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.webServer.stop()
    }
}

@Composable
fun Home(webServer: WebServer) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val snackbarCoroutineScope = rememberCoroutineScope()
    var filename by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<ByteArray?>(null) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                filename = getFilenameFromUri(context, uri)
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
                data = inputStream.readBytes()
                webServer.exportFileName = filename
                webServer.exportFileData = data!!.clone()
            }
        }

    Scaffold(scaffoldState = scaffoldState) {
        Column(Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                var serverState by remember { mutableStateOf(webServer.wasStarted()) }

                Text("Serve", style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = serverState,
                    onCheckedChange = {
                        if (serverState) {
                            webServer.stop()
                        } else {
                            try {
                                webServer.start()
                            } catch (e: IOException) {
                                snackbarCoroutineScope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(e.message.toString())
                                }
                                e.printStackTrace()
                            }
                        }
                        serverState = webServer.isAlive
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(filename ?: "No selected", style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.weight(1f))
                Button(onClick = { launcher.launch(arrayOf("*/*")) }) {
                    Text("Select File")
                }
            }
            if (filename != null && (filename!!.endsWith(".png") || filename!!.endsWith(".jpg")) && data != null) Image(
                bitmap = BitmapFactory.decodeByteArray(data!!, 0, data!!.size).asImageBitmap(),
                contentDescription = ""
            )
        }
    }
}

@SuppressLint("Recycle")
fun getFilenameFromUri(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
    var filename: String? = null
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            filename = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
        }
        cursor.close()
    }
    return filename
}
