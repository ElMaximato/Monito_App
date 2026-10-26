package com.monito.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.monito.mobile.presentation.navigation.AppNavHost
import com.monito.mobile.presentation.theme.MonitoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as MonitoApplication).container
        setContent {
            MonitoTheme {
                AppNavHost(container = container)
            }
        }
    }
}
