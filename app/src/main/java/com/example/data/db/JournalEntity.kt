package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val type: String // "BIRTH", "EVOLUTION", "MUTATION", "CARE", "UPGRADE", "MINIGAME"
)
