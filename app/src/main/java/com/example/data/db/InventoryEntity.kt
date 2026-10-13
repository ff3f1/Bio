package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String, // "WATER", "FOOD", "TOOL"
    val count: Int,
    val price: Int,
    val effectValue: Float,
    val xpReward: Int,
    val description: String,
    val iconEmoji: String
)
