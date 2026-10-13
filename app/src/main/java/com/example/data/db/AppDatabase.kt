package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PetEntity::class,
        UpgradeEntity::class,
        InventoryItemEntity::class,
        JournalEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bioSimDao(): BioSimDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "biosim_garden_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.bioSimDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: BioSimDao) {
                // Initial Pet
                val now = System.currentTimeMillis()
                dao.insertOrUpdatePet(
                    PetEntity(
                        id = 1,
                        name = "Цветик",
                        level = 1,
                        experience = 0,
                        evolutionStage = "SPORE",
                        mutationType = "NONE",
                        hydration = 0.85f,
                        nutrition = 0.80f,
                        lightLevel = 0.85f,
                        hygiene = 0.95f,
                        energy = 0.90f,
                        happiness = 0.88f,
                        isSleeping = false,
                        isLampOn = true,
                        bioCredits = 150,
                        birthTimestamp = now,
                        lastUpdatedTimestamp = now,
                        activeBiome = "NEON_OASIS",
                        unlockedBiomes = "NEON_OASIS"
                    )
                )

                // Initial Upgrades
                val initialUpgrades = listOf(
                    UpgradeEntity(
                        id = "auto_irrigation",
                        name = "Капельная авто-лейка",
                        description = "Каждые 30 секунд аккуратно поливает росточек на 2%.",
                        level = 0,
                        maxLevel = 5,
                        baseCost = 120,
                        iconKey = "WATER",
                        effectValuePerLevel = 0.02f
                    ),
                    UpgradeEntity(
                        id = "uv_regulator",
                        name = "Солнечное зеркальце",
                        description = "Направляет тёплые лучи на клумбу: +20% опыта за заботу.",
                        level = 0,
                        maxLevel = 5,
                        baseCost = 160,
                        iconKey = "LIGHT",
                        effectValuePerLevel = 0.20f
                    ),
                    UpgradeEntity(
                        id = "nano_filter",
                        name = "Мульчирование почвы",
                        description = "Покрывает землю опилками, снижая сорняки на 25%.",
                        level = 0,
                        maxLevel = 5,
                        baseCost = 180,
                        iconKey = "CLEAN",
                        effectValuePerLevel = 0.25f
                    ),
                    UpgradeEntity(
                        id = "bio_accumulator",
                        name = "Уютная плетёная ограда",
                        description = "Защищает от ветра: сохраняет энергию и даёт +25% монеток.",
                        level = 0,
                        maxLevel = 5,
                        baseCost = 220,
                        iconKey = "ENERGY",
                        effectValuePerLevel = 0.25f
                    ),
                    UpgradeEntity(
                        id = "mutagen_stimulator",
                        name = "Цветочная селекция",
                        description = "Повышает шанс вырастить редкую красивую разновидность.",
                        level = 0,
                        maxLevel = 3,
                        baseCost = 350,
                        iconKey = "MUTATION",
                        effectValuePerLevel = 0.33f
                    )
                )
                dao.insertUpgrades(initialUpgrades)

                // Initial Inventory Items
                val initialInventory = listOf(
                    InventoryItemEntity(
                        id = "pure_water",
                        name = "Родниковая вода",
                        category = "WATER",
                        count = 999,
                        price = 0,
                        effectValue = 0.25f,
                        xpReward = 8,
                        description = "Свежая прохладная водичка из садового колодца.",
                        iconEmoji = "💧"
                    ),
                    InventoryItemEntity(
                        id = "mineral_water",
                        name = "Талая лесная вода",
                        category = "WATER",
                        count = 5,
                        price = 25,
                        effectValue = 0.45f,
                        xpReward = 18,
                        description = "Обогащена природными минералами, укрепляет стебелёк.",
                        iconEmoji = "🍶"
                    ),
                    InventoryItemEntity(
                        id = "nano_dew",
                        name = "Роса с лепестков",
                        category = "WATER",
                        count = 2,
                        price = 55,
                        effectValue = 0.70f,
                        xpReward = 35,
                        description = "Собранная на рассвете хрустальная роса.",
                        iconEmoji = "💎"
                    ),
                    InventoryItemEntity(
                        id = "bio_compost",
                        name = "Питательный перегной",
                        category = "FOOD",
                        count = 999,
                        price = 0,
                        effectValue = 0.25f,
                        xpReward = 8,
                        description = "Мягкая плодородная земля с полезными микроэлементами.",
                        iconEmoji = "🌱"
                    ),
                    InventoryItemEntity(
                        id = "solar_nectar",
                        name = "Ягодный нектар",
                        category = "FOOD",
                        count = 4,
                        price = 30,
                        effectValue = 0.50f,
                        xpReward = 22,
                        description = "Сладкое варенье из лесной земляники для быстрого роста.",
                        iconEmoji = "🍓"
                    ),
                    InventoryItemEntity(
                        id = "starlight_spores",
                        name = "Золотая пыльца",
                        category = "FOOD",
                        count = 1,
                        price = 70,
                        effectValue = 0.80f,
                        xpReward = 45,
                        description = "Медовая цветочная пыльца, дарящая огромную радость.",
                        iconEmoji = "✨"
                    )
                )
                dao.insertInventory(initialInventory)

                // First Journal Note
                dao.insertJournal(
                    JournalEntryEntity(
                        title = "Посадка цветочка",
                        message = "В тёплый глиняный горшочек бережно посажен зелёный росточек Цветик. Начинается заботливый уход!",
                        type = "BIRTH"
                    )
                )
            }
        }
    }
}
