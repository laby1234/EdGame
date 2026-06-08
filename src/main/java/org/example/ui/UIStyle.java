package org.example.ui;

import javafx.scene.paint.Color;

public class UIStyle {
    // Fonts
    public static final String TITLE_FONT = "fonts/title_font.ttf";
    public static final String MENU_FONT = "fonts/menu_font.ttf";

    public static final Color ACCENT_COLOR = Color.web("#FFD700");         // Gold
    public static final Color TEXT_COLOR = Color.web("#F5DEB3");           // Wheat


    // Background gradient - Medieval Dark Stone
    public static final String GRADIENT_BG = "-fx-background-color: linear-gradient(to bottom, #654321 0%, #3d2817 100%);";
    public static final String DARK_GRADIENT_BG = "-fx-background-color: linear-gradient(to bottom, #2c1810 0%, #1a0f08 100%);";

    // Button styles - Medieval Stone/Wood with Border
    public static final String BUTTON_BASE = "-fx-font-size: 18px; " +
            "-fx-text-fill: #F5DEB3; " +
            "-fx-font-weight: bold; " +
            "-fx-text-shadow: 2px 2px 0px #1C0F08;";

    public static final String BUTTON_NORMAL = BUTTON_BASE +
            " -fx-padding: 12px 40px; " +
            " -fx-background-color: linear-gradient(to bottom, #8B4513 0%, #654321 100%); " +
            " -fx-background-radius: 0; " +
            " -fx-border-radius: 0; " +
            " -fx-border-width: 3; " +
            " -fx-border-color: #FFD700 #654321 #654321 #FFD700; " +
            " -fx-border-style: solid;";

    public static final String BUTTON_HOVER = BUTTON_BASE +
            " -fx-padding: 12px 40px; " +
            " -fx-background-color: linear-gradient(to bottom, #FFD700 0%, #DAA520 100%); " +
            " -fx-text-fill: #2c1810; " +
            " -fx-background-radius: 0; " +
            " -fx-border-radius: 0; " +
            " -fx-border-width: 3; " +
            " -fx-border-color: #FFD700 #8B4513 #8B4513 #FFD700; " +
            " -fx-border-style: solid; " +
            " -fx-effect: dropshadow(gaussian, #FFD700, 5, 0.5, 0, 0);";

    public static final String BUTTON_PRESSED = BUTTON_BASE +
            " -fx-padding: 14px 38px 10px 42px; " +
            " -fx-background-color: linear-gradient(to bottom, #654321 0%, #8B4513 100%); " +
            " -fx-background-radius: 0; " +
            " -fx-border-radius: 0; " +
            " -fx-border-width: 3; " +
            " -fx-border-color: #654321 #FFD700 #FFD700 #654321;";

    // Slider styles - Medieval
    public static final String SLIDER_STYLE = "-fx-control-inner-background: #8B4513; " +
            "-fx-text-fill: #F5DEB3; " +
            "-fx-border-color: #FFD700;";


    // Panel border - Medieval Stone
    public static final String PANEL_BORDER = "-fx-border-color: #FFD700; " +
            "-fx-border-width: 2; " +
            "-fx-padding: 20; " +
            "-fx-border-radius: 0; " +
            "-fx-background-color: linear-gradient(to bottom, #3d2817 0%, #2c1810 100%);";
}


