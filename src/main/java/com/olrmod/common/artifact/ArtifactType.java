package com.olrmod.common.artifact;

/**
 * Перечисление типов артефактов
 */
public enum ArtifactType {
    MEDUSA("medusa"),             // Медуза
    SOUL("soul"),                 // Душа
    FLASH("flash"),               // Вспышка
    STONE_FLOWER("stone_flower"), // Каменный цветок
    NIGHT_STAR("night_star"),     // Ночная звезда
    EMPTY("empty"),               // Пустышка
    FIREBALL("fireball"),         // Огненный шар
    CRYSTAL_THORN("crystal_thorn"),// Кристальная колючка
    MOMS_BEADS("moms_beads");     // Мамины бусы
    
    private final String name;
    
    ArtifactType(String name) {
        this.name = name;
    }
    
    /**
     * Получить название типа артефакта
     * 
     * @return название типа артефакта
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получить тип артефакта по названию
     * 
     * @param name название типа артефакта
     * @return тип артефакта или null если не найден
     */
    public static ArtifactType getByName(String name) {
        for (ArtifactType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
} 