package TechCraft.lumenmesh.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Парсер и хранилище макетов GUI из gui_layouts.json.
 * Предоставляет координаты и размеры виджетов для каждого экрана.
 */
public class GuiLayout {
    private static final Gson GSON = new GsonBuilder().create();
    
    private final String namespace;
    private final List<ScreenLayout> screens;

    public GuiLayout(String namespace, List<ScreenLayout> screens) {
        this.namespace = namespace;
        this.screens = screens;
    }

    public String getNamespace() {
        return namespace;
    }

    public List<ScreenLayout> getScreens() {
        return screens;
    }

    public ScreenLayout getScreen(String screenId) {
        return screens.stream()
            .filter(s -> s.id().equals(screenId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Загружает макеты из JSON строки.
     */
    public static GuiLayout fromJson(String json) {
        JsonObject root = GSON.fromJson(json, JsonObject.class);
        String namespace = root.get("namespace").getAsString();
        
        List<ScreenLayout> screens = new ArrayList<>();
        JsonArray screensArray = root.getAsJsonArray("screens");
        
        for (int i = 0; i < screensArray.size(); i++) {
            JsonObject screenObj = screensArray.get(i).getAsJsonObject();
            screens.add(ScreenLayout.fromJson(screenObj));
        }
        
        return new GuiLayout(namespace, screens);
    }

    /**
     * Макет экрана GUI.
     */
    public record ScreenLayout(
        String id,
        ResourceLocation texture,
        int width,
        int height,
        List<Widget> widgets
    ) {
        public static ScreenLayout fromJson(JsonObject obj) {
            String id = obj.get("id").getAsString();
            ResourceLocation texture = ResourceLocation.parse(obj.get("texture").getAsString());
            int width = obj.get("width").getAsInt();
            int height = obj.get("height").getAsInt();
            
            List<Widget> widgets = new ArrayList<>();
            JsonArray widgetsArray = obj.getAsJsonArray("widgets");
            
            for (int i = 0; i < widgetsArray.size(); i++) {
                widgets.add(Widget.fromJson(widgetsArray.get(i).getAsJsonObject()));
            }
            
            return new ScreenLayout(id, texture, width, height, widgets);
        }
    }

    /**
     * Виджет на экране GUI.
     */
    public record Widget(
        WidgetType type,
        String id,
        int x,
        int y,
        int width,
        int height,
        int index // Для слотов
    ) {
        public static Widget fromJson(JsonObject obj) {
            WidgetType type = WidgetType.fromString(obj.get("type").getAsString());
            String id = obj.has("id") ? obj.get("id").getAsString() : "";
            int x = obj.get("x").getAsInt();
            int y = obj.get("y").getAsInt();
            int w = obj.get("w").getAsInt();
            int h = obj.get("h").getAsInt();
            int index = obj.has("index") ? obj.get("index").getAsInt() : -1;
            
            return new Widget(type, id, x, y, w, h, index);
        }
    }

    /**
     * Типы виджетов.
     */
    public enum WidgetType {
        STATUS_BAR,
        BUTTON,
        MODE_BUTTON,
        PERMISSION_BUTTON,
        SORT_BUTTON,
        SEARCH_FIELD,
        PLAYER_SLOT,
        HOTBAR_SLOT,
        MACHINE_SLOT,
        INPUT_SLOT,
        OUTPUT_SLOT,
        BLUEPRINT_INPUT,
        BLUEPRINT_OUTPUT,
        PATTERN_SLOT,
        CRAFTING_SLOT,
        CRAFTING_RESULT,
        GHOST_FILTER_SLOT,
        VIRTUAL_ITEM_SLOT;

        public static WidgetType fromString(String str) {
            return switch (str) {
                case "status_bar" -> STATUS_BAR;
                case "button" -> BUTTON;
                case "mode_button" -> MODE_BUTTON;
                case "permission_button" -> PERMISSION_BUTTON;
                case "sort_button" -> SORT_BUTTON;
                case "search_field" -> SEARCH_FIELD;
                case "player_slot" -> PLAYER_SLOT;
                case "hotbar_slot" -> HOTBAR_SLOT;
                case "machine_slot" -> MACHINE_SLOT;
                case "input_slot" -> INPUT_SLOT;
                case "output_slot" -> OUTPUT_SLOT;
                case "blueprint_input" -> BLUEPRINT_INPUT;
                case "blueprint_output" -> BLUEPRINT_OUTPUT;
                case "pattern_slot" -> PATTERN_SLOT;
                case "crafting_slot" -> CRAFTING_SLOT;
                case "crafting_result" -> CRAFTING_RESULT;
                case "ghost_filter_slot" -> GHOST_FILTER_SLOT;
                case "virtual_item_slot" -> VIRTUAL_ITEM_SLOT;
                default -> throw new IllegalArgumentException("Unknown widget type: " + str);
            };
        }
    }

    /**
     * Прямоугольная область для GUI.
     */
    public record GuiRect(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        }
    }
}
