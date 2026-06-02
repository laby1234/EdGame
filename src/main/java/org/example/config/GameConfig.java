package org.example.config;

public class GameConfig {
    // Window settings
    // Use 16:9 aspect ratio by default (e.g. 1280x720)
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final String WINDOW_TITLE = "EdGame";

    // World (side-scrolling) size
    public static final int WORLD_WIDTH = WINDOW_WIDTH * 3;
    public static final int WORLD_HEIGHT = WINDOW_HEIGHT;

    // Ground
    public static final int GROUND_Y = 600;
    public static final int GROUND_HEIGHT = 100;

    // Player
    public static final int PLAYER_SIZE = 40;
    public static final int PLAYER_START_X = 100;
    public static final int PLAYER_START_Y = 300;
    // Values are expressed per second and scaled by delta time.
    public static final double PLAYER_SPEED = 180;
    public static final double PLAYER_GRAVITY = 1080;
    public static final double PLAYER_JUMP_STRENGTH = -720;

    // Tile size (used for block-based terrain)
    public static final int TILE_SIZE = 40;
}
