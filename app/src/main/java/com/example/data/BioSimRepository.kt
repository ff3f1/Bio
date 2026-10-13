package com.example.data

import com.example.data.db.AppDatabase
import com.example.data.db.InventoryItemEntity
import com.example.data.db.JournalEntryEntity
import com.example.data.db.PetEntity
import com.example.data.db.UpgradeEntity
import com.example.model.BiomeTheme
import com.example.model.EvolutionStage
import com.example.model.GameBalance
import com.example.model.MutationType
import com.example.model.PetMood
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class BioSimRepository(private val database: AppDatabase) {
    private val dao = database.bioSimDao()

    val petStateFlow: Flow<PetEntity?> = dao.getPetStateFlow()
    val upgradesFlow: Flow<List<UpgradeEntity>> = dao.getAllUpgradesFlow()
    val inventoryFlow: Flow<List<InventoryItemEntity>> = dao.getAllInventoryFlow()
    val journalFlow: Flow<List<JournalEntryEntity>> = dao.getRecentJournalFlow()

    suspend fun tickSimulation(elapsedSeconds: Long) {
        val pet = dao.getPetState() ?: return
        val upgrades = dao.getAllUpgradesFlow() // we can fetch list
        val autoWaterLvl = dao.getUpgrade("auto_irrigation")?.level ?: 0
        val bioFilterLvl = dao.getUpgrade("nano_filter")?.level ?: 0
        val accumulatorLvl = dao.getUpgrade("bio_accumulator")?.level ?: 0

        // Decay rates per minute:
        // Hydration: ~3.5% per minute base
        val moistureRetainBonus = if (pet.mutationType == MutationType.DRYAD_EMERALD.id) 0.35f else 0.0f
        val netHydrationDecayPerSec = (0.035f / 60f) * (1f - moistureRetainBonus)
        val autoWaterGainPerSec = (autoWaterLvl * 0.02f) / 30f // from upgrade

        var newHydration = pet.hydration - (netHydrationDecayPerSec * elapsedSeconds) + (autoWaterGainPerSec * elapsedSeconds)
        newHydration = newHydration.coerceIn(0.05f, 1.0f) // never instantly 0

        // Nutrition: ~3.0% per minute base
        val netNutritionDecayPerSec = 0.030f / 60f
        var newNutrition = (pet.nutrition - netNutritionDecayPerSec * elapsedSeconds).coerceIn(0.05f, 1.0f)

        // Hygiene / Parasites: decreases slowly, slowed down by bio filter
        val filterBonus = bioFilterLvl * 0.20f
        val netHygieneDecayPerSec = (0.015f / 60f) * (1f - filterBonus)
        var newHygiene = (pet.hygiene - netHygieneDecayPerSec * elapsedSeconds).coerceIn(0.10f, 1.0f)

        // Energy:
        var newEnergy = pet.energy
        if (pet.isSleeping) {
            // Recharges 10% per minute
            val rechargePerSec = 0.10f / 60f
            newEnergy = (newEnergy + rechargePerSec * elapsedSeconds).coerceIn(0.0f, 1.0f)
        } else {
            val energyDrainPerSec = (0.018f / 60f) * (1f - accumulatorLvl * 0.15f)
            newEnergy = (newEnergy - energyDrainPerSec * elapsedSeconds).coerceIn(0.05f, 1.0f)
        }

        // Happiness calculation
        val healthAverage = (newHydration + newNutrition + newHygiene + newEnergy) / 4f
        var newHappiness = (pet.happiness * 0.95f + healthAverage * 0.05f).coerceIn(0.1f, 1.0f)

        // Passive bio-credits if thriving
        var newCredits = pet.bioCredits
        if (healthAverage > 0.70f && !pet.isSleeping && elapsedSeconds >= 30) {
            val creditMultiplier = if (pet.mutationType == MutationType.PLASMA_SOLAR.id) 1.5f else 1.0f
            val bonusCredits = ((elapsedSeconds / 30) * 2 * creditMultiplier).toInt()
            newCredits += bonusCredits
        }

        val updatedPet = pet.copy(
            hydration = newHydration,
            nutrition = newNutrition,
            hygiene = newHygiene,
            energy = newEnergy,
            happiness = newHappiness,
            bioCredits = newCredits,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdatePet(updatedPet)
    }

    suspend fun waterPet(itemId: String): Boolean {
        val pet = dao.getPetState() ?: return false
        val item = dao.getInventoryItem(itemId) ?: return false
        if (item.count <= 0 && item.id != "pure_water") return false

        if (item.id != "pure_water") {
            dao.updateInventoryItem(item.copy(count = item.count - 1))
        }

        val uvLampBonus = (dao.getUpgrade("uv_regulator")?.level ?: 0) * 0.20f
        val earnedXp = (item.xpReward * (1f + uvLampBonus)).toInt()

        val newHydration = (pet.hydration + item.effectValue).coerceAtMost(1.0f)
        val newHappy = (pet.happiness + 0.08f).coerceAtMost(1.0f)

        processExperienceGain(
            pet.copy(
                hydration = newHydration,
                happiness = newHappy,
                totalWaterCount = pet.totalWaterCount + 1,
                lastUpdatedTimestamp = System.currentTimeMillis()
            ),
            earnedXp,
            "Полив: ${item.name}"
        )
        return true
    }

    suspend fun feedPet(itemId: String): Boolean {
        val pet = dao.getPetState() ?: return false
        val item = dao.getInventoryItem(itemId) ?: return false
        if (item.count <= 0 && item.id != "bio_compost") return false

        if (item.id != "bio_compost") {
            dao.updateInventoryItem(item.copy(count = item.count - 1))
        }

        val uvLampBonus = (dao.getUpgrade("uv_regulator")?.level ?: 0) * 0.20f
        val earnedXp = (item.xpReward * (1f + uvLampBonus)).toInt()

        val newNutrition = (pet.nutrition + item.effectValue).coerceAtMost(1.0f)
        val newHappy = (pet.happiness + 0.10f).coerceAtMost(1.0f)

        processExperienceGain(
            pet.copy(
                nutrition = newNutrition,
                happiness = newHappy,
                totalFeedCount = pet.totalFeedCount + 1,
                lastUpdatedTimestamp = System.currentTimeMillis()
            ),
            earnedXp,
            "Подкормка: ${item.name}"
        )
        return true
    }

    suspend fun petSymbiont(): Int {
        val pet = dao.getPetState() ?: return 0
        val earnedXp = 5
        val newHappy = (pet.happiness + 0.06f).coerceAtMost(1.0f)
        val bonusCredit = if (Random.nextFloat() > 0.6f) 1 else 0

        processExperienceGain(
            pet.copy(
                happiness = newHappy,
                totalPetCount = pet.totalPetCount + 1,
                bioCredits = pet.bioCredits + bonusCredit,
                lastUpdatedTimestamp = System.currentTimeMillis()
            ),
            earnedXp,
            null
        )
        return earnedXp
    }

    suspend fun cleanParasites(): Boolean {
        val pet = dao.getPetState() ?: return false
        val earnedXp = 15
        val earnedCredits = 10

        processExperienceGain(
            pet.copy(
                hygiene = 1.0f,
                happiness = (pet.happiness + 0.12f).coerceAtMost(1.0f),
                bioCredits = pet.bioCredits + earnedCredits,
                lastUpdatedTimestamp = System.currentTimeMillis()
            ),
            earnedXp,
            "Санация террариума завершена (+10 монет)"
        )
        return true
    }

    suspend fun toggleLamp(): Boolean {
        val pet = dao.getPetState() ?: return false
        val newLamp = !pet.isLampOn
        val newLight = if (newLamp) 0.85f else 0.20f
        dao.insertOrUpdatePet(pet.copy(isLampOn = newLamp, lightLevel = newLight))
        return newLamp
    }

    suspend fun toggleSleep(): Boolean {
        val pet = dao.getPetState() ?: return false
        val newSleep = !pet.isSleeping
        val newLamp = if (newSleep) false else true
        val newLight = if (newSleep) 0.15f else 0.80f
        dao.insertOrUpdatePet(
            pet.copy(
                isSleeping = newSleep,
                isLampOn = newLamp,
                lightLevel = newLight,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        )
        dao.insertJournal(
            JournalEntryEntity(
                title = if (newSleep) "Фаза сна включена" else "Симбионт пробудился",
                message = if (newSleep) "Террариум затемнен для ускоренного восстановления сил." else "Симбионт полон сил и готов к фотосинтезу.",
                type = "CARE"
            )
        )
        return newSleep
    }

    suspend fun buyUpgrade(upgradeId: String): Boolean {
        val pet = dao.getPetState() ?: return false
        val upgrade = dao.getUpgrade(upgradeId) ?: return false
        if (upgrade.isMaxed) return false
        val cost = upgrade.currentCost
        if (pet.bioCredits < cost) return false

        dao.insertOrUpdatePet(pet.copy(bioCredits = pet.bioCredits - cost))
        dao.updateUpgrade(upgrade.copy(level = upgrade.level + 1))

        dao.insertJournal(
            JournalEntryEntity(
                title = "Улучшение: ${upgrade.name}",
                message = "Уровень повышен до ${upgrade.level + 1}. Террариум модернизирован.",
                type = "UPGRADE"
            )
        )
        return true
    }

    suspend fun buyInventoryItem(itemId: String): Boolean {
        val pet = dao.getPetState() ?: return false
        val item = dao.getInventoryItem(itemId) ?: return false
        if (pet.bioCredits < item.price) return false

        dao.insertOrUpdatePet(pet.copy(bioCredits = pet.bioCredits - item.price))
        dao.updateInventoryItem(item.copy(count = item.count + 1))
        return true
    }

    suspend fun unlockBiome(biomeId: String): Boolean {
        val pet = dao.getPetState() ?: return false
        val biome = BiomeTheme.values().find { it.id == biomeId } ?: return false
        if (pet.bioCredits < biome.unlockCost || pet.level < biome.unlockLevel) return false

        val currentUnlocked = pet.unlockedBiomes.split(",").toMutableSet()
        currentUnlocked.add(biomeId)

        dao.insertOrUpdatePet(
            pet.copy(
                bioCredits = pet.bioCredits - biome.unlockCost,
                unlockedBiomes = currentUnlocked.joinToString(","),
                activeBiome = biomeId
            )
        )
        dao.insertJournal(
            JournalEntryEntity(
                title = "Открыт новый биом: ${biome.titleRu}",
                message = "Террариум перенесен в новую экосистему: ${biome.descriptionRu}",
                type = "EVOLUTION"
            )
        )
        return true
    }

    suspend fun selectBiome(biomeId: String) {
        val pet = dao.getPetState() ?: return
        if (pet.unlockedBiomes.contains(biomeId)) {
            dao.insertOrUpdatePet(pet.copy(activeBiome = biomeId))
        }
    }

    suspend fun awardMiniGameReward(score: Int, gameType: String): Pair<Int, Int> {
        val pet = dao.getPetState() ?: return Pair(0, 0)
        // calculate credits and XP
        val creditsEarned = (score * 0.8f).toInt().coerceAtLeast(5)
        val xpEarned = (score * 0.6f).toInt().coerceAtLeast(10)

        processExperienceGain(
            pet.copy(
                bioCredits = pet.bioCredits + creditsEarned,
                happiness = (pet.happiness + 0.15f).coerceAtMost(1.0f)
            ),
            xpEarned,
            "Мини-игра: $gameType (Счет: $score)"
        )
        return Pair(creditsEarned, xpEarned)
    }

    private suspend fun processExperienceGain(pet: PetEntity, gainedXp: Int, journalMsg: String?) {
        var currentExp = pet.experience + gainedXp
        var currentLevel = pet.level
        var currentStage = pet.evolutionStage
        var currentMutation = pet.mutationType
        var leveledUp = false

        while (true) {
            val reqXp = GameBalance.getRequiredXp(currentLevel)
            if (currentExp >= reqXp) {
                currentExp -= reqXp
                currentLevel += 1
                leveledUp = true
            } else {
                break
            }
        }

        val newStage = EvolutionStage.fromLevel(currentLevel).name
        val stageChanged = newStage != currentStage

        if (stageChanged) {
            currentStage = newStage
            // Potential mutation check
            if (currentMutation == MutationType.NONE.id && currentLevel >= 5) {
                val mutagenLvl = dao.getUpgrade("mutagen_stimulator")?.level ?: 0
                val roll = Random.nextFloat() + (mutagenLvl * 0.20f)
                if (roll > 0.40f) {
                    val possibleMutations = listOf(
                        MutationType.DRYAD_EMERALD,
                        MutationType.PLASMA_SOLAR,
                        MutationType.CRYSTAL_QUARTZ
                    )
                    val chosen = possibleMutations.random()
                    currentMutation = chosen.id
                    dao.insertJournal(
                        JournalEntryEntity(
                            title = "Прорыв мутации: ${chosen.titleRu}!",
                            message = "Организм приобрел редкую мутацию: ${chosen.bonusRu}",
                            type = "MUTATION"
                        )
                    )
                }
            }
            dao.insertJournal(
                JournalEntryEntity(
                    title = "Эволюция! Новая форма: ${EvolutionStage.valueOf(newStage).titleRu}",
                    message = "Симбионт достиг фазы ${EvolutionStage.valueOf(newStage).subtitleRu}. Открыты новые возможности!",
                    type = "EVOLUTION"
                )
            )
        } else if (leveledUp) {
            dao.insertJournal(
                JournalEntryEntity(
                    title = "Уровень повышен до $currentLevel!",
                    message = "Симбионт растет крепче и выносливее.",
                    type = "CARE"
                )
            )
        } else if (journalMsg != null) {
            // Optional care log
        }

        dao.insertOrUpdatePet(
            pet.copy(
                level = currentLevel,
                experience = currentExp,
                evolutionStage = currentStage,
                mutationType = currentMutation,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        )
    }

    fun computePetMood(pet: PetEntity): PetMood {
        return when {
            pet.isSleeping -> PetMood.SLEEPING
            pet.hygiene < 0.35f -> PetMood.SICK
            pet.hydration < 0.30f -> PetMood.THIRSTY
            pet.nutrition < 0.30f -> PetMood.HUNGRY
            pet.happiness > 0.85f && pet.hydration > 0.6f -> PetMood.THRIVING
            pet.happiness > 0.60f -> PetMood.HAPPY
            else -> PetMood.NORMAL
        }
    }
}
