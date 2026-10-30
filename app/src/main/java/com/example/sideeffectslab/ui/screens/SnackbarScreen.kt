package com.example.sideeffectslab.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 2-topshiriq: SnackbarScreen.kt
 *
 * rememberCoroutineScope ishlatilishi:
 * User hodisasi (button click) sodir bo'lganda asynchronous ish (Snackbar ko'rsatish) bajarish kerak.
 *
 * Nega bu yerda LaunchedEffect ishlatib bo'lmaydi?
 * - LaunchedEffect - bu Composable Composition faol bo'lganda (ekranga chizilayotganda) ishga tushadigan side-effect.
 * - Button onClick esa user harakati bilan chaqiriladigan oddiy callback/event handler.
 * - onClick ichida LaunchedEffect chaqirib bo'lmaydi (chunki u Composable funksiya).
 * - Shu sababli event handler ichida Coroutine ochish uchun `rememberCoroutineScope` ishlatiladi.
 */
@Composable
fun SnackbarScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var count by remember { mutableIntStateOf(0) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "2. rememberCoroutineScope (Snackbar)",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    count++
                    // Button handler ichida coroutine scope yordamida Snackbar ko'rsatamiz
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Snackbar #$count ko'rsatildi! 🚀"
                        )
                    }
                }
            ) {
                Text("Snackbar ko'rsatish")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "💡 Tushuntirish:\n" +
                            "Button.onClick bu Composable funksiya emas, balki oddiy Kotlin lambda callback'dir.\n" +
                            "LaunchedEffect Composable funksiya bo'lgani uchun uni onClick ichida chaqirib bo'lmaydi.\n" +
                            "Foydalanuvchi tugmani bosganda asinxron coroutine'ni ishga tushirish uchun rememberCoroutineScope() dan foydalaniladi.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
