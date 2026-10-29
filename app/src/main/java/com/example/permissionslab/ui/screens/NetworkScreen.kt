package com.example.permissionslab.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * 2-qadam: NetworkScreen.kt (Normal permission)
 *
 * Normal ruxsatnomalar (INTERNET, ACCESS_NETWORK_STATE) shaxsiy ma'lumotlarga
 * va xavfsizlikka xavf tug'dirmaydi. Shu sababli ular uchun RUNTIME DIALOG so'rash
 * shart emas! Faqat AndroidManifest.xml faylida e'lon qilish yetarli.
 */
@Composable
fun NetworkScreen() {
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(checkInitialNetworkState(context)) }

    // ConnectivityManager orqali tarmoq holatini real-vaqtda kuzatish
    DisposableEffect(context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = true
            }

            override fun onLost(network: Network) {
                isOnline = checkInitialNetworkState(context)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
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
            text = "Normal Permission (Network)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isOnline) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = if (isOnline) "Status: Online 🌐" else "Status: Offline 🚫",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Eslatma: Normal ruxsatnomalar (INTERNET, ACCESS_NETWORK_STATE) uchun runtime dialog talab qilinmaydi. Manifestda e'lon qilish yetarli.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun checkInitialNetworkState(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
