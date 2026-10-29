package com.example.permissionslab.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * 5-qadam: LocationScreen.kt (One-time permission)
 *
 * RequestMultiplePermissions yordamida ACCESS_FINE_LOCATION va ACCESS_COARSE_LOCATION
 * birga so'raladi. Bir martalik ("Only this time") ruxsatlar ilova fonga o'tsa bekor bo'ladi.
 * Shu sababli ON_RESUME vaqtida ruxsatlarni qayta tekshiramiz.
 */
@Composable
fun LocationScreen() {
    val context = LocalContext.current

    var permissionsState by remember {
        mutableStateOf(getCurrentLocationPermissions(context))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsState = result
    }

    // Foydalanuvchi ilovani fonga olib o'tib qaytganda ("Only this time" bekor bo'lishi sababli)
    // ON_RESUME ichida ruxsatlarni qayta tekshiramiz
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionsState = getCurrentLocationPermissions(context)
    }

    val fineGranted = permissionsState[Manifest.permission.ACCESS_FINE_LOCATION] == true
    val coarseGranted = permissionsState[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Multiple & One-time Permission (Location)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ACCESS_FINE_LOCATION (Aniq): ${if (fineGranted) "Berilgan ✅" else "Rad etilgan ❌"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ACCESS_COARSE_LOCATION (Taxminiy): ${if (coarseGranted) "Berilgan ✅" else "Rad etilgan ❌"}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        ) {
            Text("Joylashuv ruxsatlarini so'rash 📍")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Eslatma: 'Only this time' (Bir martalik) ruxsati berilganda, ilova fonga o'tishi bilan ruxsat avtomatik bekor qilinadi. LifecycleEventEffect(ON_RESUME) uni qayta aniqlaydi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getCurrentLocationPermissions(context: Context): Map<String, Boolean> {
    return mapOf(
        Manifest.permission.ACCESS_FINE_LOCATION to (
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ),
        Manifest.permission.ACCESS_COARSE_LOCATION to (
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    )
}
