package com.monito.wear

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monito.wear.presentation.pet.PetScreen
import com.monito.wear.presentation.pet.PetViewModel
import com.monito.wear.presentation.pet.petViewModelFactory
import com.monito.wear.presentation.theme.MonitoWearTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as WearApplication).container

        setContent {
            // Permisos de sensores corporales y de actividad; el ritmo cardíaco arranca al responder.
            val permissions = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions(),
            ) { container.heartRate.start() }

            LaunchedEffect(Unit) {
                permissions.launch(
                    arrayOf(Manifest.permission.BODY_SENSORS, Manifest.permission.ACTIVITY_RECOGNITION),
                )
            }

            MonitoWearTheme {
                val viewModel: PetViewModel = viewModel(factory = petViewModelFactory(container))
                PetScreen(viewModel)
            }
        }
    }
}
