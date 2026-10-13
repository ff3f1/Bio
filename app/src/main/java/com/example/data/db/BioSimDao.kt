package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BioSimDao {
    @Query("SELECT * FROM pet_state WHERE id = 1 LIMIT 1")
    fun getPetStateFlow(): Flow<PetEntity?>

    @Query("SELECT * FROM pet_state WHERE id = 1 LIMIT 1")
    suspend fun getPetState(): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePet(pet: PetEntity)

    // Upgrades
    @Query("SELECT * FROM upgrades")
    fun getAllUpgradesFlow(): Flow<List<UpgradeEntity>>

    @Query("SELECT * FROM upgrades WHERE id = :id LIMIT 1")
    suspend fun getUpgrade(id: String): UpgradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpgrades(upgrades: List<UpgradeEntity>)

    @Update
    suspend fun updateUpgrade(upgrade: UpgradeEntity)

    // Inventory
    @Query("SELECT * FROM inventory")
    fun getAllInventoryFlow(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory WHERE id = :id LIMIT 1")
    suspend fun getInventoryItem(id: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateInventoryItem(item: InventoryItemEntity)

    // Journal
    @Query("SELECT * FROM journal ORDER BY timestamp DESC LIMIT 50")
    fun getRecentJournalFlow(): Flow<List<JournalEntryEntity>>

    @Insert
    suspend fun insertJournal(entry: JournalEntryEntity)
}
