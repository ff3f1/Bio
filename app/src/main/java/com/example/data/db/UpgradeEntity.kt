package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upgrades")
data class UpgradeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val level: Int = 0,
    val maxLevel: Int = 5,
    val baseCost: Int,
    val iconKey: String,
    val effectValuePerLevel: Float
) {
    val currentCost: Int
        get() = (baseCost * Math.pow(1.65, level.toDouble())).toInt()

    val isMaxed: Boolean
        get() = level >= maxLevel
}
