package com.example.sideeffectslab.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 1-topshiriq: SplashScreen.kt
 *
 * rememberUpdatedState ishlatilishi:
 * Qachonki Composable ichidagi uzoq davom etadigan effekt (masalan LaunchedEffect(Unit) ichidagi 2s timer)
 * recomposition paytida qayta boshlanmasligi kerak bo'lsa, lekin u chaqiradigan callback (onTimeout)
 * har doim eng so'nggi holatga (state/lambda) ega bo'lishi lozim bo'lganda `rememberUpdatedState` ishlatiladi.
 */
@Composable
fun SplashScreen() {
    var version by remember { mutableIntStateOf(0) }
    var resultCorrect by remember { mutableStateOf("Kutilmoqda (2 soniya)...") }
    var resultIncorrect by remember { mutableStateOf("Kutilmoqda...") }

    // Har 500ms da version qiymati oshib turadi, bu har safar yangi lambda uzatilishiga olib keladi
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            version++
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
            text = "1. rememberUpdatedState (Splash Screen)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Version counter (har 500ms da oshadi): $version",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Correct variant: rememberUpdatedState
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "✅ To'g'ri variant (rememberUpdatedState):",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = resultCorrect)

                // LandingScreen Correct
                LandingScreenCorrect(
                    onTimeout = {
                        resultCorrect = "Muvaffaqiyatli o'tdi! (Version: $version da chaqirildi)"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Incorrect variant: LaunchedEffect(onTimeout)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "❌ Noto'g'ri variant (LaunchedEffect(onTimeout)):",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = resultIncorrect)

                // LandingScreen Incorrect
                LandingScreenIncorrect(
                    onTimeout = {
                        resultIncorrect = "Tugadi! (Version: $version)"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Izoh: Agar LaunchedEffect(onTimeout) qilsak, har safar parent recomposition bo'lib yangi lambda kelganda key o'zgaradi va 2 soniyalik taymer bekor qilinib, qaytadan boshlanadi. Natijada timeout hech qachon tugamaydi! rememberUpdatedState(onTimeout) esa timer-ni qayta boshlamasdan, eng so'nggi lambdani saqlab qoladi.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * To'g'ri variant: rememberUpdatedState yordamida lambdani saqlaymiz.
 * LaunchedEffect(Unit) 2 soniya davomida uzluksiz ishlaydi va tugagach eng yangi lambdani chaqiradi.
 */
@Composable
fun LandingScreenCorrect(onTimeout: () -> Unit) {
    // onTimeout o'zgarsa ham, eng so'nggi qiymatini saqlab qoladi
    val currentOnTimeout by rememberUpdatedState(onTimeout)

    LaunchedEffect(Unit) {
        delay(2000)
        currentOnTimeout()
    }
}

/**
 * Noto'g'ri variant: LaunchedEffect(onTimeout)
 * har safar onTimeout lambda obyekti o'zgarganda taymer noldan qayta boshlanadi!
 */
@Composable
fun LandingScreenIncorrect(onTimeout: () -> Unit) {
    LaunchedEffect(onTimeout) {
        delay(2000)
        onTimeout()
    }
}
