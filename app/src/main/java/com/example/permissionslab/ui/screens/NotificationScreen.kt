package com.example.permissionslab.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
 * 6-qadam: NotificationScreen.kt (API version check)
 *
 * POST_NOTIFICATIONS faqat Android 13 (API 33, TIRAMISU) va undan yuqori versiyalarda
 * runtime ruxsatnoma hisoblanadi. Undan pastki versiyalarda bu ruxsat avtomatik berilgan.
 */
@Composable
fun NotificationScreen() {
    val context = LocalContext.current

    var isGranted by remember { mutableStateOf(checkNotificationPermission(context)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted = granted
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isGranted = checkNotificationPermission(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "API Version Check (Notification)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isGranted) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = if (isGranted) "Bildirishnomalarga ruxsat bor 🔔" else "Ruxsat yo'q 🔕",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // API 33 (TIRAMISU) yoki undan yuqorilarda runtime so'rov yuborish
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // API < 33 bo'lsa so'rov o'tkazib yuboriladi (avtomatik granted)
                    isGranted = true
                }
            }
        ) {
            Text("Notification ruxsatini so'rash 🔔")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Qurilma Android versiyasi: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n" +
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        "Sizning qurilmangizda runtime dialog talab qilinadi (API >= 33)."
                    else
                        "Sizning qurilmangizda so'rov o'tkazib yuboriladi, chunki API < 33 da bildirishnoma avtomatik ruxsat etilgan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // API < 33 da POST_NOTIFICATIONS runtime ruxsat emas, avtomatik true
        true
    }
}
