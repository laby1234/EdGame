package org.example.config;

public class GameConfig {
    // Window settings
    // Use 16:9 aspect ratio by default (e.g. 1280x720)
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final String WINDOW_TITLE = "EdGame";

    // Ground
    public static final int GROUND_Y = 500;
    public static final int GROUND_HEIGHT = 100;

    // Player
    public static final int PLAYER_SIZE = 40;
    public static final int PLAYER_START_X = 100;
    public static final int PLAYER_START_Y = 300;
    public static final double PLAYER_SPEED = 3;
    public static final double PLAYER_GRAVITY = 0.3;
    public static final double PLAYER_JUMP_STRENGTH = -12;

    // Tile size (used for block-based terrain)
    public static final int TILE_SIZE = 40;
}
