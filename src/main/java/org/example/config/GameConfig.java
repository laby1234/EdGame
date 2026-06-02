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
    public static final int PLAYER_MAX_HEALTH = 100;
    public static final int PLAYER_CONTACT_DAMAGE = 15;
    public static final double PLAYER_DAMAGE_COOLDOWN = 0.8;
    public static final int SWORD_DAMAGE = 35;
    public static final double SWORD_RANGE = 75;
    public static final double SWORD_COOLDOWN = 0.35;
    public static final int ARROW_DAMAGE = 25;
    public static final double ARROW_SPEED = 520;
    public static final double ARROW_COOLDOWN = 0.55;
    public static final double BOW_MAX_CHARGE_TIME = 1.1;
    public static final double PLAYER_KNOCKBACK_X = 220;
    public static final double PLAYER_KNOCKBACK_Y = -260;

    // Enemy
    public static final int ENEMY_WIDTH = 44;
    public static final int ENEMY_HEIGHT = 46;
    public static final int ENEMY_MAX_HEALTH = 80;
    public static final double ENEMY_SPEED = 75;
    public static final double ENEMY_AGGRO_RANGE = 320;
    public static final double ENEMY_ATTACK_RANGE = 42;
    public static final double ENEMY_ATTACK_COOLDOWN = 1.0;
    public static final double ENEMY_KNOCKBACK = 130;

    // Pickups and score
    public static final int COIN_SCORE = 10;
    public static final int ENEMY_SCORE = 50;
    public static final int HEART_HEAL = 25;
    public static final int PICKUP_SIZE = 24;

    // Tile size (used for block-based terrain)
    public static final int TILE_SIZE = 40;
}
