package dev.euryperez.later

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.euryperez.later.ui.LaterNavHost
import dev.euryperez.later.ui.theme.LaterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaterTheme {
                LaterNavHost()
            }
        }
    }
}
