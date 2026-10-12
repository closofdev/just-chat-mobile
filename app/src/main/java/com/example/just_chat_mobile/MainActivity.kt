package com.example.just_chat_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.just_chat_mobile.ui.ChatScreen
import com.example.just_chat_mobile.ui.theme.JustchatmobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JustchatmobileTheme {
                ChatScreen()
            }
        }
    }
}
