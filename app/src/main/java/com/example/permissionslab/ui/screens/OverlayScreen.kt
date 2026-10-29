package com.example.permissionslab.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
 * 4-qadam: OverlayScreen.kt (Special permission)
 *
 * Special ruxsatlar (SYSTEM_ALERT_WINDOW) butun tizim xavfsizligiga ta'sir qilgani uchun
 * oddiy runtime dialog orqali berilmaydi. Foydalanuvchi Sozlamalar (Settings) sahifasiga
 * o'tib qo'lda yoqishi kerak.
 */
@Composable
fun OverlayScreen() {
    val context = LocalContext.current
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // Sozlamalar oynasidan ilovaga qaytganda holatni avtomatik yangilash uchun ON_RESUME ishlatiladi
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        canDrawOverlays = Settings.canDrawOverlays(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Special Permission (Overlay)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (canDrawOverlays) "Overlay ruxsati berilgan 🟢" else "Overlay ruxsati o'chirilgan 🔴",
                style = MaterialTheme.typography.titleMedium
            )

            Switch(
                checked = canDrawOverlays,
                onCheckedChange = {
                    // Switch bosilganda Sozlamalar (Settings) sahifasini ochish
                    openOverlaySettings(context)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Nega ON_RESUME kerak?\nSozlamalar oynasidan ilovaga qaytganda Android callback yubormaydi. Shu sababli ON_RESUME hodisasi sodir bo'lganda canDrawOverlays(context) holati qayta o'qiladi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun openOverlaySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    context.startActivity(intent)
}
