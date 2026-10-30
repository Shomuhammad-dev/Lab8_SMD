package com.example.sideeffectslab.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 5-topshiriq: ScrollScreen.kt
 *
 * 1. derivedStateOf:
 * Tez-tez o'zgaradigan state-lardan (masalan scroll pozitsiyasi pixelma-pixel o'zgarganda)
 * yangi sodda boolean state hosil qilishda keraksiz recomposition-larning oldini oladi.
 *
 * 2. snapshotFlow:
 * Compose State-ni Kotlin StateFlow/Flow-ga aylantirib, map, filter, distinctUntilChanged kabi
 * operatorlarni qo'llash imkonini beradi.
 */
private const val TAG = "ScrollScreen"

@Composable
fun ScrollScreen() {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 1. derivedStateOf bilan - faqat boolean qiymat o'zgargandagina recomposition bo'ladi (0 -> >0)
    val showButtonDerived by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    // Recomposition sonini hisoblash
    var derivedRecompositionCount by remember { mutableIntStateOf(0) }
    var directRecompositionCount by remember { mutableIntStateOf(0) }

    // 2. snapshotFlow orqali 0-elementdan o'tganda faqat 1 marta analytics yuborish
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> index > 0 }
            .distinctUntilChanged()
            .filter { isScrolled -> isScrolled } // Faqat true bo'lganda
            .collect {
                Log.d(TAG, "Analytics Event: Foydalanuvchi pastga scroll qildi (0-elementdan o'tdi)!")
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "5. derivedStateOf & snapshotFlow",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recomposition ko'rsatkichlari
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "derivedStateOf variant recomposition: $derivedRecompositionCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "To'g'ridan-to'g'ri variant recomposition: $directRecompositionCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(100) { index ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Element #$index",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // derivedStateOf bilan tugma
            if (showButtonDerived) {
                // SideEffect yordamida ushbu blok recomposition sonini sanaymiz
                SideEffect { derivedRecompositionCount++ }

                Button(
                    onClick = {
                        scope.launch { listState.animateScrollToItem(0) }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text("Tepaga ⬆️")
                }
            }

            // Taqqoslash uchun: To'g'ridan-to'g'ri o'qiydigan variant
            val showButtonDirect = listState.firstVisibleItemIndex > 0
            if (showButtonDirect) {
                SideEffect { directRecompositionCount++ }
            }
        }
    }
}
