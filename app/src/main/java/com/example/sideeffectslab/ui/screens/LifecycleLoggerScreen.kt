package com.example.sideeffectslab.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 3-topshiriq: LifecycleLoggerScreen.kt
 *
 * DisposableEffect ishlatilishi:
 * Clean-up (resurslarni tozalash) talab qilinadigan side-effectlar uchun ishlatiladi.
 * Composable ekrandan yo'qolganda (decomposed bo'lganda) `onDispose` bloki avtomatik chaqiriladi.
 */
private const val TAG = "LifecycleLoggerScreen"

@Composable
fun LifecycleLoggerScreen() {
    var isLoggerVisible by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "3. DisposableEffect (Lifecycle Logger)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Lifecycle Component'ni ko'rsatish:")
            Switch(
                checked = isLoggerVisible,
                onCheckedChange = { isLoggerVisible = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoggerVisible) {
            LifecycleObserverCard()
        } else {
            Text(
                text = "Observer o'chirildi (onDispose ishga tushdi). Logcat-ni tekshiring!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun LifecycleObserverCard(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    // DisposableEffect yordamida lifecycle observer-ni bog'laymiz
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    Log.d(TAG, "Lifecycle Event: ON_START")
                }
                Lifecycle.Event.ON_STOP -> {
                    Log.d(TAG, "Lifecycle Event: ON_STOP")
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        Log.d(TAG, "Observer lifecycle-ga qo'shildi")

        // Composable ekrandan yo'qolganda yoki key o'zgarganda tozalaymiz
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            Log.d(TAG, "onDispose: Observer lifecycle-dan olib tashlandi!")
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🟢 Lifecycle Observer Faol",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ilovani fonga olib o'ting yoki yuqoridagi Switch-ni o'chiring. Logcat faylida '$TAG' tegi ostida eventlarni kuzating.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
