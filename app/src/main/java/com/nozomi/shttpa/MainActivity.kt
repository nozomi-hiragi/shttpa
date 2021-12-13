package com.nozomi.shttpa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nozomi.shttpa.ui.theme.ShttpaTheme
import kotlinx.coroutines.launch
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
    val scaffoldState = rememberScaffoldState()
    val snackbarCoroutineScope = rememberCoroutineScope()

    Scaffold(scaffoldState = scaffoldState) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.padding(16.dp)) {
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
        }
    }
}
