package TechCraft.item;

/**
 * Перечисление типов материалов в моде TechCraft.
 * Используется для унифицированной регистрации предметов из разных материалов.
 */
public enum MaterialType {
    TIN("tin"),
    IRON("iron"),
    GOLD("gold"),
    COPPER("copper"),
    STEEL("steel"),
    BRONZE("bronze"),
    NICKEL("nickel"),
    SILVER("silver"),
    PRISMITE("prismite"),
    QUANTUM("quantum");

    private final String name;

    MaterialType(String name) {
        this.name = name;
    }

    /**
     * Возвращает имя материала для использования в регистрации.
     * @return имя материала
     */
    public String getName() {
        return name;
    }
}
