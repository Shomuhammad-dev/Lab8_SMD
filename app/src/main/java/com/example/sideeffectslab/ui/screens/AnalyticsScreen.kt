package com.example.sideeffectslab.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 4-topshiriq: AnalyticsScreen.kt
 *
 * SideEffect ishlatilishi:
 * Compose holatini (State) Compose boshqarmaydigan tashqi obyektlar (non-Compose objects, masalan Analytics, Crashlytics, DB)
 * bilan sinxronlashtirish uchun ishlatiladi.
 * SideEffect har bir MUVAFFAQIYATLI recomposition bo'lgandan so'ng darhol ishga tushadi.
 */
private const val TAG = "AnalyticsScreen"

// Compose-dan tashqaridagi oddiy obyekt
object Analytics {
    var userType: String = "Guest"
}

@Composable
fun AnalyticsScreen() {
    var userType by remember { mutableStateOf("Guest") }
    var recompositionCount by remember { mutableIntStateOf(0) }

    // SideEffect har safar recomposition muvaffaqiyatli tugagach ishlaydi
    SideEffect {
        recompositionCount++
        Analytics.userType = userType
        Log.d(
            TAG,
            "SideEffect bajarildi! Recomposition #$recompositionCount -> Analytics.userType: ${Analytics.userType}"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "4. SideEffect (Analytics Sync)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Joriy User Type: ${Analytics.userType}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SideEffect ishlash soni (Recomposition count): $recompositionCount",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { userType = "Guest 👤" }) {
                Text("Guest")
            }
            Button(onClick = { userType = "Premium 👑" }) {
                Text("Premium")
            }
            Button(onClick = { userType = "Admin 🛠️" }) {
                Text("Admin")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "💡 Tushuntirish:\n" +
                        "SideEffect har bir recomposition yakunlangach tashqi Analytics obyektiga yangi userType ni yozadi.\n" +
                        "Bu Compose state va tashqi tizimlar o'rtasida ma'lumotlar bir xil turishini ta'minlaydi.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
