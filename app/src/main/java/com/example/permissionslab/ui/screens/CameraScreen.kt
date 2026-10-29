package com.example.permissionslab.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * 3-qadam: CameraScreen.kt (Runtime permission)
 *
 * Runtime ruxsatlar (CAMERA) foydalanuvchi ma'lumotlariga kirish imkonini bergani uchun
 * ish rejimida (runtime) dialog orqali so'raladi.
 */
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()

    var isGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var shouldShowRationale by remember {
        mutableStateOf(
            activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } ?: false
        )
    }

    var isPermanentlyDenied by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted = granted
        if (!granted) {
            val rationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } ?: false
            shouldShowRationale = rationale
            // Agar rationale false va granted false bo'lsa -> Permanently denied (Don't ask again)
            isPermanentlyDenied = !rationale
        } else {
            shouldShowRationale = false
            isPermanentlyDenied = false
        }
    }

    // Sozlamalarga o'tib qaytganda holatni qayta tekshirish
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        isGranted = granted
        if (granted) {
            shouldShowRationale = false
            isPermanentlyDenied = false
        } else if (activity != null) {
            shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Runtime Permission (Camera)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            // 1. Ruxsat berilgan holat
            isGranted -> {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(
                        text = "Ruxsat berilgan ✅\nKamera tayyor!",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            // 2. Rationale holati (Bir marta rad etilgan)
            shouldShowRationale -> {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Kamera ishlashi uchun ruxsat zarur! 📸\nRasm va video olish uchun kameralarga kirish ruxsatini berishingiz kerak.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                            Text("Ruxsat berish")
                        }
                    }
                }
            }

            // 3. Qolgan holatlar (Hali so'ralmagan yoki 2 marta rad etilib, Sozlamalarga yo'naltirish kerak)
            else -> {
                if (isPermanentlyDenied) {
                    Text(
                        text = "Ruxsat rad etilgan (Don't ask again). Sozlamalar bo'limidan ruxsatni yoqing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { openAppSettings(context) }) {
                        Text("Sozlamalarni ochish ⚙️")
                    }
                } else {
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Ruxsat so'rash 📷")
                    }
                }
            }
        }
    }
}

// Activity topish uchun yordamchi funksiya
private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

// Settings.ACTION_APPLICATION_DETAILS_SETTINGS Intent orqali Sozlamalar oynasini ochish
private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
