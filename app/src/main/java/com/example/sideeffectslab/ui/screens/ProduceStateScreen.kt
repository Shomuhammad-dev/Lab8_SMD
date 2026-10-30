package com.example.sideeffectslab.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 6-topshiriq: ProduceStateScreen.kt
 *
 * produceState ishlatilishi:
 * Non-Compose manbalarni (masalan Flow, LiveData, yoki suspend funksiyalarni) Compose State-ga aylantirib beradi.
 * produceState Coroutine Scope yaratadi va value-ni o'zgartirganda Composable UI avtomatik yangilanadi.
 */

// Rasmlar yuklanishi natijasi uchun sealed interfeys
sealed interface ImageResult {
    object Loading : ImageResult
    data class Success(val message: String) : ImageResult
    data class Error(val error: String) : ImageResult
}

// Soxta tarmoq repozitoriyasi
private suspend fun fetchImageFromNetwork(url: String): ImageResult {
    delay(1000) // 1 soniyalik soxta yuklanish taymeri
    return if (url.lowercase().endsWith(".png")) {
        ImageResult.Success("Rasm muvaffaqiyatli yuklandi: $url 🖼️")
    } else {
        ImageResult.Error("Xatolik: URL '.png' bilan tugashi shart! ($url)")
    }
}

/**
 * produceState yordamida suspend funksiyani Compose State-ga o'tkazish
 */
@Composable
fun loadNetworkImage(url: String): State<ImageResult> {
    return produceState<ImageResult>(initialValue = ImageResult.Loading, key1 = url) {
        value = ImageResult.Loading
        value = fetchImageFromNetwork(url)
    }
}

@Composable
fun ProduceStateScreen() {
    var inputUrl by remember { mutableStateOf("https://example.com/logo.png") }
    var currentUrl by remember { mutableStateOf(inputUrl) }

    // produceState chaqiruvi
    val imageResultState by loadNetworkImage(currentUrl)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "6. produceState (Image Loader)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = inputUrl,
            onValueChange = { inputUrl = it },
            label = { Text("Rasm URL kiriting") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { currentUrl = inputUrl },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Rasmni yuklash 📥")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // State holatiga qarab UI ko'rsatish
        when (val result = imageResultState) {
            is ImageResult.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rasm yuklanmoqda...")
                }
            }
            is ImageResult.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is ImageResult.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result.error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
