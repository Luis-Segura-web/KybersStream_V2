package com.kybers.stream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.kybers.stream.presentation.navigation.KybersStreamNavigation
import com.kybers.stream.presentation.theme.KybersStreamTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KybersStreamTheme {
                KybersStreamNavigation(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}