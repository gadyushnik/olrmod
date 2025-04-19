package com.olrmod.common.anomaly;

/**
 * Перечисление доступных типов аномалий
 */
public enum AnomalyType {
    MOSQUITO("mosquito"),
    MINCER("mincer"),
    GALANTINE("galantine"),
    ELECTRO("electro"),
    CHEMICAL("chemical"),
    GRAVITATIONAL("gravitational"),
    THERMAL("thermal"),
    BURNING_FUZZ("burning_fuzz"),
    RUSTY_HAIR("rusty_hair"),
    BLACK_NEEDLES("black_needles");
    
    private final String name;
    
    AnomalyType(String name) {
        this.name = name;
    }
    
    /**
     * Получить название типа аномалии
     * 
     * @return название типа аномалии
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получить тип аномалии по названию
     * 
     * @param name название типа аномалии
     * @return тип аномалии или null если не найден
     */
    public static AnomalyType getByName(String name) {
        for (AnomalyType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
} 