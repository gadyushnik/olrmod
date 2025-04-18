package com.olrmod.common.detector;

/**
 * Перечисление типов детекторов
 */
public enum DetectorType {
    ANOMALY("anomaly"),     // Детектор аномалий
    RADIATION("radiation"), // Детектор радиации (счетчик Гейгера)
    ARTIFACT("artifact");   // Детектор артефактов
    
    private final String name;
    
    DetectorType(String name) {
        this.name = name;
    }
    
    /**
     * Получить название типа детектора
     * 
     * @return название типа детектора
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получить тип детектора по названию
     * 
     * @param name название типа детектора
     * @return тип детектора или null если не найден
     */
    public static DetectorType getByName(String name) {
        for (DetectorType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
} 