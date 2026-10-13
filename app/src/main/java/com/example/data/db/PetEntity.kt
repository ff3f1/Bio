package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_state")
data class PetEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Симбионт Альфа",
    val level: Int = 1,
    val experience: Int = 0,
    val evolutionStage: String = "SPORE",
    val mutationType: String = "NONE",
    val hydration: Float = 0.85f,      // 0.0 to 1.0 (water)
    val nutrition: Float = 0.80f,      // 0.0 to 1.0 (fertilizer/food)
    val lightLevel: Float = 0.70f,     // 0.0 to 1.0 (light lamp)
    val hygiene: Float = 0.95f,        // 0.0 to 1.0 (cleanliness/parasites)
    val energy: Float = 0.90f,         // 0.0 to 1.0 (stamina/sleep)
    val happiness: Float = 0.85f,      // 0.0 to 1.0 (affection/petting)
    val isSleeping: Boolean = false,
    val isLampOn: Boolean = true,
    val bioCredits: Int = 120,
    val totalFeedCount: Int = 0,
    val totalWaterCount: Int = 0,
    val totalPetCount: Int = 0,
    val birthTimestamp: Long = System.currentTimeMillis(),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val activeBiome: String = "NEON_OASIS",
    val unlockedBiomes: String = "NEON_OASIS"
)
