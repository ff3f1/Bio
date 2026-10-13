package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BioSimRepository
import com.example.data.db.AppDatabase
import com.example.data.db.InventoryItemEntity
import com.example.data.db.JournalEntryEntity
import com.example.data.db.PetEntity
import com.example.data.db.UpgradeEntity
import com.example.model.PetMood
import com.example.util.FeedbackManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BioSimViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = BioSimRepository(database)
    val feedbackManager = FeedbackManager(application)

    // Current navigation screen tab
    private val _currentScreenTab = MutableStateFlow(0)
    val currentScreenTab: StateFlow<Int> = _currentScreenTab.asStateFlow()

    // Event notifications (e.g. snackbar message)
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    val petState: StateFlow<PetEntity?> = repository.petStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val moodState: StateFlow<PetMood> = petState.map { pet ->
        if (pet != null) repository.computePetMood(pet) else PetMood.NORMAL
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PetMood.NORMAL)

    val upgradesState: StateFlow<List<UpgradeEntity>> = repository.upgradesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryState: StateFlow<List<InventoryItemEntity>> = repository.inventoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalState: StateFlow<List<JournalEntryEntity>> = repository.journalFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // 1. Initial offline progression calculation
        viewModelScope.launch {
            val pet = database.bioSimDao().getPetState()
            if (pet != null) {
                val elapsedSeconds = ((System.currentTimeMillis() - pet.lastUpdatedTimestamp) / 1000L).coerceAtLeast(0)
                if (elapsedSeconds > 10) {
                    repository.tickSimulation(elapsedSeconds)
                    if (elapsedSeconds > 120) {
                        _uiEvents.emit("С возвращением! Прошло ${elapsedSeconds / 60} мин. Биосфера обновлена.")
                    }
                }
            }
        }

        // 2. Real-time active simulation loop (runs every 5 seconds)
        viewModelScope.launch {
            while (true) {
                delay(5000)
                repository.tickSimulation(5)
            }
        }
    }

    fun selectScreenTab(tabIndex: Int) {
        feedbackManager.vibrateTap()
        _currentScreenTab.value = tabIndex
    }

    fun waterPet(itemId: String) {
        viewModelScope.launch {
            val success = repository.waterPet(itemId)
            if (success) {
                feedbackManager.vibrateWater()
                _uiEvents.emit("💧 Цветик напился чистой водички!")
            }
        }
    }

    fun feedPet(itemId: String) {
        viewModelScope.launch {
            val success = repository.feedPet(itemId)
            if (success) {
                feedbackManager.vibrateFeed()
                _uiEvents.emit("🍓 Сладкое угощение пошло на пользу!")
            }
        }
    }

    fun petSymbiont() {
        viewModelScope.launch {
            feedbackManager.vibratePet()
            repository.petSymbiont()
        }
    }

    fun cleanParasites() {
        viewModelScope.launch {
            feedbackManager.vibrateTap()
            val success = repository.cleanParasites()
            if (success) {
                _uiEvents.emit("✂️ Все сорняки выполоты! Награда: +10 монеток 🪙")
            }
        }
    }

    fun toggleLamp() {
        viewModelScope.launch {
            feedbackManager.vibrateTap()
            val lampOn = repository.toggleLamp()
            _uiEvents.emit(if (lampOn) "☀️ Тёплое солнышко светит на клумбу!" else "☁️ Тень: росточек отдыхает от полуденного солнца")
        }
    }

    fun toggleSleep() {
        viewModelScope.launch {
            feedbackManager.vibrateTap()
            val sleeping = repository.toggleSleep()
            _uiEvents.emit(if (sleeping) "🌙 Баиньки! Цветик сладко спит под звёздами" else "☀️ Доброе утро! Цветик проснулся с улыбкой!")
        }
    }

    fun buyUpgrade(upgradeId: String) {
        viewModelScope.launch {
            val success = repository.buyUpgrade(upgradeId)
            if (success) {
                feedbackManager.vibrateLevelUp()
                _uiEvents.emit("🌿 Садовое улучшение установлено!")
            } else {
                feedbackManager.vibrateTap()
                _uiEvents.emit("Недостаточно монеток")
            }
        }
    }

    fun buyInventoryItem(itemId: String) {
        viewModelScope.launch {
            val success = repository.buyInventoryItem(itemId)
            if (success) {
                feedbackManager.vibrateTap()
                _uiEvents.emit("Угощение положено в садовую корзинку")
            } else {
                feedbackManager.vibrateTap()
                _uiEvents.emit("Недостаточно монеток")
            }
        }
    }

    fun unlockBiome(biomeId: String) {
        viewModelScope.launch {
            val success = repository.unlockBiome(biomeId)
            if (success) {
                feedbackManager.vibrateLevelUp()
                _uiEvents.emit("🌸 Новый прекрасный уголок сада открыт!")
            }
        }
    }

    fun selectBiome(biomeId: String) {
        viewModelScope.launch {
            feedbackManager.vibrateTap()
            repository.selectBiome(biomeId)
            _uiEvents.emit("Садовый фон изменён")
        }
    }

    fun awardMiniGame(score: Int, gameName: String) {
        viewModelScope.launch {
            if (score > 0) {
                feedbackManager.vibrateLevelUp()
                val rewards = repository.awardMiniGameReward(score, gameName)
                _uiEvents.emit("🎉 $gameName: заработано +${rewards.first} 🪙 и +${rewards.second} XP!")
            }
        }
    }
}
