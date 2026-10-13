package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BestiaryScreen
import com.example.ui.screens.BiomesScreen
import com.example.ui.screens.LaboratoryScreen
import com.example.ui.screens.MainTerrariumScreen
import com.example.ui.screens.MiniGamesScreen
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BioSimViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BioSimApp()
            }
        }
    }
}

@Composable
fun BioSimApp(viewModel: BioSimViewModel = viewModel()) {
    val pet by viewModel.petState.collectAsStateWithLifecycle()
    val mood by viewModel.moodState.collectAsStateWithLifecycle()
    val upgrades by viewModel.upgradesState.collectAsStateWithLifecycle()
    val inventory by viewModel.inventoryState.collectAsStateWithLifecycle()
    val journal by viewModel.journalState.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentScreenTab.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BackHandler(enabled = currentTab != 0) {
        viewModel.selectScreenTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BioSimBottomNavigation(
                currentTab = currentTab,
                onTabSelect = { viewModel.selectScreenTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF0FDF4))
        ) {
            val currentPet = pet
            if (currentPet == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MeadowGreen)
                }
            } else {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabContent"
                ) { tab ->
                    when (tab) {
                        0 -> MainTerrariumScreen(
                            pet = currentPet,
                            mood = mood,
                            inventory = inventory,
                            onPetTouched = { viewModel.petSymbiont() },
                            onWaterSelect = { viewModel.waterPet(it) },
                            onFeedSelect = { viewModel.feedPet(it) },
                            onCleanClick = { viewModel.cleanParasites() },
                            onToggleLamp = { viewModel.toggleLamp() },
                            onToggleSleep = { viewModel.toggleSleep() },
                            onNavigateToLab = { viewModel.selectScreenTab(1) },
                            snackbarHostState = snackbarHostState
                        )
                        1 -> LaboratoryScreen(
                            pet = currentPet,
                            upgrades = upgrades,
                            inventory = inventory,
                            onBuyUpgrade = { viewModel.buyUpgrade(it) },
                            onBuyItem = { viewModel.buyInventoryItem(it) }
                        )
                        2 -> MiniGamesScreen(
                            onGameFinished = { score, gameName ->
                                viewModel.awardMiniGame(score, gameName)
                            }
                        )
                        3 -> BestiaryScreen(
                            pet = currentPet,
                            journalEntries = journal
                        )
                        4 -> BiomesScreen(
                            pet = currentPet,
                            onSelectBiome = { viewModel.selectBiome(it) },
                            onUnlockBiome = { viewModel.unlockBiome(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BioSimBottomNavigation(
    currentTab: Int,
    onTabSelect: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = MeadowGreen,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = currentTab == 0,
            onClick = { onTabSelect(0) },
            icon = { Icon(Icons.Default.LocalFlorist, contentDescription = "Цветик", modifier = Modifier.size(24.dp)) },
            label = { Text("Цветик", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MeadowGreen,
                selectedTextColor = MeadowGreen,
                indicatorColor = Color(0xFFDCFCE7),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_tab_terrarium")
        )

        NavigationBarItem(
            selected = currentTab == 1,
            onClick = { onTabSelect(1) },
            icon = { Icon(Icons.Default.Storefront, contentDescription = "Мастерская", modifier = Modifier.size(24.dp)) },
            label = { Text("Лавка", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MeadowGreen,
                selectedTextColor = MeadowGreen,
                indicatorColor = Color(0xFFDCFCE7),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_tab_lab")
        )

        NavigationBarItem(
            selected = currentTab == 2,
            onClick = { onTabSelect(2) },
            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Игры", modifier = Modifier.size(24.dp)) },
            label = { Text("Игры", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MeadowGreen,
                selectedTextColor = MeadowGreen,
                indicatorColor = Color(0xFFDCFCE7),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_tab_games")
        )

        NavigationBarItem(
            selected = currentTab == 3,
            onClick = { onTabSelect(3) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Гербарий", modifier = Modifier.size(24.dp)) },
            label = { Text("Гербарий", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MeadowGreen,
                selectedTextColor = MeadowGreen,
                indicatorColor = Color(0xFFDCFCE7),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_tab_bestiary")
        )

        NavigationBarItem(
            selected = currentTab == 4,
            onClick = { onTabSelect(4) },
            icon = { Icon(Icons.Default.Yard, contentDescription = "Сады", modifier = Modifier.size(24.dp)) },
            label = { Text("Сады", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MeadowGreen,
                selectedTextColor = MeadowGreen,
                indicatorColor = Color(0xFFDCFCE7),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_tab_biomes")
        )
    }
}
