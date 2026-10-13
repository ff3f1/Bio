package com.example.model

enum class EvolutionStage(
    val titleRu: String,
    val subtitleRu: String,
    val minLevel: Int,
    val baseHealth: Float,
    val descriptionRu: String
) {
    SPORE(
        titleRu = "Росточек-Малыш",
        subtitleRu = "Фаза 1: Первые листики",
        minLevel = 1,
        baseHealth = 100f,
        descriptionRu = "Крошечный весёлый росточек в керамическом горшочке. Радуется тёплому солнышку и чистой водичке."
    ),
    BUD(
        titleRu = "Бутончик-Колокольчик",
        subtitleRu = "Фаза 2: Цветочный бутон",
        minLevel = 5,
        baseHealth = 180f,
        descriptionRu = "Подрастающий цветочек с задорными ручками-листиками и нежными шёлковыми лепестками."
    ),
    BLOOM(
        titleRu = "Цветущий Флорик",
        subtitleRu = "Фаза 3: Пышное цветение",
        minLevel = 10,
        baseHealth = 260f,
        descriptionRu = "Великолепный распустившийся цветок! Вокруг порхают бабочки, а лепестки источают сладкий аромат."
    ),
    APEX(
        titleRu = "Королевский Садовый Дух",
        subtitleRu = "Фаза 4: Великий Цветок",
        minLevel = 20,
        baseHealth = 400f,
        descriptionRu = "Величественный волшебный хранитель цветущего сада с золотой цветочной короной и радужной пыльцой."
    );

    companion object {
        fun fromLevel(level: Int): EvolutionStage {
            return when {
                level >= 20 -> APEX
                level >= 10 -> BLOOM
                level >= 5 -> BUD
                else -> SPORE
            }
        }
    }
}

enum class MutationType(
    val id: String,
    val titleRu: String,
    val bonusRu: String,
    val descriptionRu: String
) {
    NONE(
        id = "NONE",
        titleRu = "Обычный Садовый",
        bonusRu = "Стандартный рост",
        descriptionRu = "Естественный природный цветок без редких мутаций."
    ),
    DRYAD_EMERALD(
        id = "DRYAD_EMERALD",
        titleRu = "Изумрудный Клевер",
        bonusRu = "+35% удержание влаги",
        descriptionRu = "Листики в форме счастливого четырёхлистника удерживают утреннюю росу."
    ),
    PLASMA_SOLAR(
        id = "PLASMA_SOLAR",
        titleRu = "Солнечный Подсолнух",
        bonusRu = "+50% сбор монеток",
        descriptionRu = "Яркие золотые лепестки ловят каждый солнечный лучик."
    ),
    CRYSTAL_QUARTZ(
        id = "CRYSTAL_QUARTZ",
        titleRu = "Бархатная Роза",
        bonusRu = "Стойкость к сорнякам +40%",
        descriptionRu = "Крепкий стебель и ароматные лепестки защищают клумбу от вредителей."
    )
}

enum class BiomeTheme(
    val id: String,
    val titleRu: String,
    val unlockLevel: Int,
    val unlockCost: Int,
    val descriptionRu: String
) {
    NEON_OASIS(
        id = "NEON_OASIS",
        titleRu = "Солнечная Поляна",
        unlockLevel = 1,
        unlockCost = 0,
        descriptionRu = "Зелёный луг с ромашками, пушистыми облаками, сияющим солнышком и порхающими бабочками."
    ),
    CRYSTAL_CAVERN(
        id = "CRYSTAL_CAVERN",
        titleRu = "Сад Цветущей Сакуры",
        unlockLevel = 5,
        unlockCost = 250,
        descriptionRu = "Розовый весенний сад, лепестки на тёплом ветру и золотой солнечный закат."
    ),
    COSMIC_DOME(
        id = "COSMIC_DOME",
        titleRu = "Уютная Оранжерея",
        unlockLevel = 10,
        unlockCost = 600,
        descriptionRu = "Светлая теплица с деревянными полочками, виноградной лозой и тёплыми лучами через стекло."
    )
}

enum class PetMood(
    val titleRu: String,
    val iconEmoji: String,
    val colorHex: Long
) {
    THRIVING("Цветёт и благоухает", "🌸", 0xFF16A34A),
    HAPPY("Греется на солнышке", "☀️", 0xFFEAB308),
    NORMAL("Хорошо себя чувствует", "🌱", 0xFF0284C7),
    THIRSTY("Хочет пить из леечки", "💧", 0xFF0284C7),
    HUNGRY("Ждёт сладкой подкормки", "🍓", 0xFFEA580C),
    SICK("Нужно прополоть сорняки", "✂️", 0xFFDC2626),
    SLEEPING("Сладко спит под луной", "💤", 0xFF7C3AED)
}

object GameBalance {
    fun getRequiredXp(level: Int): Int {
        return 100 + (level - 1) * 80 + (level * level * 15)
    }
}
