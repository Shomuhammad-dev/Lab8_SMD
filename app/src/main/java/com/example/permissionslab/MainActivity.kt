package com.example.permissionslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.permissionslab.ui.screens.*
import com.example.sideeffectslab.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    var mainSection by remember { mutableIntStateOf(1) } // 0: Permissions, 1: Side Effects
    var selectedTab by remember { mutableIntStateOf(0) }

    val permissionTabs = listOf("Network", "Camera", "Overlay", "Location", "Notification")
    val sideEffectTabs = listOf("1.Splash", "2.Snackbar", "3.Lifecycle", "4.Analytics", "5.Scroll", "6.ProduceState")

    val currentTabs = if (mainSection == 0) permissionTabs else sideEffectTabs

    Scaffold(
        topBar = {
            Column {
                // Section selector: Lab 8 vs Lab 9
                TabRow(selectedTabIndex = mainSection) {
                    Tab(
                        selected = mainSection == 0,
                        onClick = { mainSection = 0; selectedTab = 0 },
                        text = { Text("Lab 8: Permissions") }
                    )
                    Tab(
                        selected = mainSection == 1,
                        onClick = { mainSection = 1; selectedTab = 0 },
                        text = { Text("Lab 9: Side Effects") }
                    )
                }

                // Sub-tabs for screens
                ScrollableTabRow(selectedTabIndex = selectedTab) {
                    currentTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (mainSection == 0) {
                when (selectedTab) {
                    0 -> NetworkScreen()
                    1 -> CameraScreen()
                    2 -> OverlayScreen()
                    3 -> LocationScreen()
                    4 -> NotificationScreen()
                }
            } else {
                when (selectedTab) {
                    0 -> SplashScreen()
                    1 -> SnackbarScreen()
                    2 -> LifecycleLoggerScreen()
                    3 -> AnalyticsScreen()
                    4 -> ScrollScreen()
                    5 -> ProduceStateScreen()
                }
            }
        }
    }
}
