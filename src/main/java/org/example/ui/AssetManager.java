package org.example.ui;

import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.io.InputStream;

public class AssetManager {

    private static final String ASSETS_PATH = "assets/";
    private static final String TEXTURES_PATH = ASSETS_PATH + "textures/";
    private static final String UI_PATH = TEXTURES_PATH + "ui/";

    public static final String MENU_BG = UI_PATH + "backgrounds/background.png";
    public static final String SETTINGS_BG = UI_PATH + "backgrounds/settings_bg.png";
    public static final String PAUSE_BG = UI_PATH + "backgrounds/pause_bg.png";
    public static final String CAVE_BG = UI_PATH + "backgrounds/cave_background.png";

    public static final String HUD_STATUS = UI_PATH + "hud_status.png";
    public static final String HUD_WEAPON = UI_PATH + "hud_weapon.png";

    public static final String BTN_NORMAL = UI_PATH + "buttons/btn_normal.png";
    public static final String BTN_HOVER = UI_PATH + "buttons/btn_hover.png";
    public static final String BTN_PRESSED = UI_PATH + "buttons/btn_pressed.png";

    public static final String ICON_SOUND_ON = UI_PATH + "icons/sound_on.png";
    public static final String ICON_SOUND_OFF = UI_PATH + "icons/sound_off.png";
    public static final String ICON_MUSIC_ON = UI_PATH + "icons/music_on.png";
    public static final String ICON_MUSIC_OFF = UI_PATH + "icons/music_off.png";

    public static final String CHEST_CLOSED = TEXTURES_PATH + "blocks/chest1.png";
    public static final String CHEST_OPEN = TEXTURES_PATH + "blocks/chest2.png";

    public static final double TITLE_SIZE = 48;
    public static final double HEADING_SIZE = 32;
    public static final double TEXT_SIZE = 18;
    public static final double SMALL_SIZE = 14;

    public static Font loadFont(String fontPath, double size) {
        try (InputStream is = AssetManager.class.getResourceAsStream("/" + fontPath)) {
            if (is == null) {
                System.err.println("Could not find font resource: /" + fontPath + " - using system font fallback");
                return Font.font("System", size);
            }

            Font f = Font.loadFont(is, size);
            if (f == null) {
                System.err.println("Font.loadFont returned null for: /" + fontPath + " - using system font fallback");
                return Font.font("System", size);
            }

            return f;
        } catch (java.io.IOException e) {
            System.err.println("Could not load font: " + fontPath + " - Using system font. Exception: " + e.getMessage());
            return Font.font("System", size);
        }
    }

    public static Image loadImage(String imagePath) {
        InputStream is = AssetManager.class.getResourceAsStream("/" + imagePath);
        if (is == null) {
            System.err.println("Could not load image: " + imagePath);
            return null;
        }

        Image image = new Image(is);
        if (image.isError()) {
            System.err.println("Could not load image: " + imagePath + " - " + image.getException());
            return null;
        }

        return image;
    }

    public static Font getTitleFont() {
        return loadFont(UIStyle.TITLE_FONT, TITLE_SIZE);
    }

    public static Font getHeadingFont() {
        return loadFont(UIStyle.MENU_FONT, HEADING_SIZE);
    }

    public static Font getTextFont() {
        return loadFont(UIStyle.MENU_FONT, TEXT_SIZE);
    }

    public static Font getSmallFont() {
        return loadFont(UIStyle.MENU_FONT, SMALL_SIZE);
    }
}