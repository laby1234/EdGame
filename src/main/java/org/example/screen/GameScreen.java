package org.example.screen;

import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.config.GameConfig;
import org.example.entity.EntityFactory;
import org.example.entity.player.PlayerComponent;
import org.example.ui.AssetManager;
import org.example.ui.ProfessionalButton;
import org.example.ui.UIStyle;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;

public class GameScreen extends Screen {

    private static final String DEATH_LABEL_TEXT = "YOU DIED";
    private static final String DEATH_HINT_TEXT = "Press R to restart";
    private static final DropShadow TITLE_SHADOW = createShadow();

    private Entity player;
    private PlayerComponent playerComponent;
    private Entity ground;
    private Entity background;
    private StackPane hudRoot;
    private StackPane deathOverlay;
    private Rectangle playerHealthFill;
    private Label playerHealthLabel;
    private Label weaponLabel;
    private Label scoreLabel;
    private Label combatTextLabel;
    private double combatTextTimer = 0;
    private int score = 0;

    private final List<Entity> platforms = new ArrayList<>();
    private final List<Entity> obstacles = new ArrayList<>();
    private final List<Entity> enemies = new ArrayList<>();
    private final List<Entity> pickups = new ArrayList<>();

    private final Runnable onRestartCallback;
    private final Runnable onMenuCallback;
    private final Runnable onGameOverCallback;

    public GameScreen(Runnable onRestartCallback, Runnable onMenuCallback, Runnable onGameOverCallback) {
        this.onRestartCallback = onRestartCallback;
        this.onMenuCallback = onMenuCallback;
        this.onGameOverCallback = onGameOverCallback;
    }

    @Override
    public void init() {
        getGameScene().setBackgroundColor(Color.WHITE);

        hudRoot = new StackPane();
        hudRoot.setPickOnBounds(false);
        hudRoot.setPrefWidth(GameConfig.WINDOW_WIDTH);
        hudRoot.setPrefHeight(GameConfig.WINDOW_HEIGHT);

        Label titleLabel = new Label("Forest");
        titleLabel.setFont(AssetManager.getTitleFont());
        titleLabel.setTextFill(UIStyle.ACCENT_COLOR);
        titleLabel.setEffect(TITLE_SHADOW);
        titleLabel.setPadding(new Insets(14, 0, 0, 0));
        StackPane.setAlignment(titleLabel, Pos.TOP_CENTER);

        Label hintLabel = new Label("R — restart");
        hintLabel.setText("LMB - use  |  J - sword  |  K - bow  |  R - restart");
        hintLabel.setFont(AssetManager.getSmallFont());
        hintLabel.setTextFill(UIStyle.TEXT_COLOR);
        hintLabel.setPadding(new Insets(16, 14, 0, 0));
        StackPane.setAlignment(hintLabel, Pos.TOP_RIGHT);

        HBox healthBox = createPlayerHealthHud();
        StackPane.setAlignment(healthBox, Pos.TOP_LEFT);

        combatTextLabel = createCombatTextLabel();
        StackPane.setAlignment(combatTextLabel, Pos.CENTER);

        hudRoot.getChildren().addAll(titleLabel, hintLabel, healthBox, combatTextLabel);
        getGameScene().addUINode(hudRoot);

        background = createScrollingBackground();
        ground = EntityFactory.createGround();

        spawnPlatformsAndObstacles();

        player = EntityFactory.createPlayer();
        playerComponent = EntityFactory.getPlayerComponent(player);
        playerComponent.setOnDeath(this::onPlayerDied);
        playerComponent.setOnHealthChanged(this::updatePlayerHealthHud);
        playerComponent.setOnWeaponChanged(this::updateWeaponHud);

        spawnEnemies();
        spawnPickups();

        getGameScene().getViewport().setBounds(0, 0, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        getGameScene().getViewport().bindToEntity(player, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.WINDOW_HEIGHT / 2.0);
    }

    private void spawnPlatformsAndObstacles() {
        // --- Platforms (x, y, width in tiles) ---
        platforms.add(EntityFactory.createPlatform(350,  480, 4));
        platforms.add(EntityFactory.createPlatform(600,  400, 3));
        platforms.add(EntityFactory.createPlatform(880,  460, 4));
        platforms.add(EntityFactory.createPlatform(1100, 360, 3));
        platforms.add(EntityFactory.createPlatform(1380, 440, 5));
        platforms.add(EntityFactory.createPlatform(1700, 380, 3));
        platforms.add(EntityFactory.createPlatform(1980, 450, 4));
        platforms.add(EntityFactory.createPlatform(2280, 350, 3));
        platforms.add(EntityFactory.createPlatform(2560, 420, 4));
        platforms.add(EntityFactory.createPlatform(2860, 460, 3));

        // --- Obstacles on ground ---
        int groundObstacleY = GameConfig.GROUND_Y - GameConfig.TILE_SIZE;
        obstacles.add(EntityFactory.createObstacle(230,  groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(750,  groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(1250, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(1600, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(2150, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(2700, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(3100, groundObstacleY));

        // --- Obstacles on platforms (y = platform.y - TILE_SIZE) ---
        obstacles.add(EntityFactory.createObstacle(1420, 440 - GameConfig.TILE_SIZE));
        obstacles.add(EntityFactory.createObstacle(2060, 450 - GameConfig.TILE_SIZE));
    }

    private void spawnEnemies() {
        int groundEnemyY = GameConfig.GROUND_Y - GameConfig.ENEMY_HEIGHT;
        enemies.add(EntityFactory.createEnemy(520, groundEnemyY, 120, player, playerComponent, this::addScore, this::showCombatText));
        enemies.add(EntityFactory.createEnemy(980, groundEnemyY, 140, player, playerComponent, this::addScore, this::showCombatText));
        enemies.add(EntityFactory.createEnemy(1480, groundEnemyY, 160, player, playerComponent, this::addScore, this::showCombatText));
        enemies.add(EntityFactory.createEnemy(2360, groundEnemyY, 160, player, playerComponent, this::addScore, this::showCombatText));
        enemies.add(EntityFactory.createEnemy(2880, groundEnemyY, 130, player, playerComponent, this::addScore, this::showCombatText));
        enemies.add(EntityFactory.createEnemy(1480, 440 - GameConfig.ENEMY_HEIGHT, 1464, 1530, player, playerComponent, this::addScore, this::showCombatText));
        enemies.add(EntityFactory.createEnemy(2110, 450 - GameConfig.ENEMY_HEIGHT, 1986, 2020, player, playerComponent, this::addScore, this::showCombatText));
    }

    private void spawnPickups() {
        pickups.add(EntityFactory.createCoin(380, 440, this::addScore));
        pickups.add(EntityFactory.createCoin(430, 440, this::addScore));
        pickups.add(EntityFactory.createCoin(650, 360, this::addScore));
        pickups.add(EntityFactory.createCoin(930, 420, this::addScore));
        pickups.add(EntityFactory.createCoin(1150, 320, this::addScore));
        pickups.add(EntityFactory.createCoin(1535, 400, this::addScore));
        pickups.add(EntityFactory.createCoin(1740, 340, this::addScore));
        pickups.add(EntityFactory.createCoin(1995, 410, this::addScore));
        pickups.add(EntityFactory.createCoin(2320, 310, this::addScore));
        pickups.add(EntityFactory.createCoin(2600, 380, this::addScore));
        pickups.add(EntityFactory.createCoin(2920, 420, this::addScore));

        pickups.add(EntityFactory.createHeart(1340, GameConfig.GROUND_Y - GameConfig.TILE_SIZE - GameConfig.PICKUP_SIZE - 6));
        pickups.add(EntityFactory.createHeart(2250, GameConfig.GROUND_Y - GameConfig.TILE_SIZE - GameConfig.PICKUP_SIZE - 6));
    }

    private void onPlayerDied() {
        if (onGameOverCallback != null) onGameOverCallback.run();
        showDeathOverlay();
    }

    private void showDeathOverlay() {
        deathOverlay = new StackPane();
        deathOverlay.setPrefWidth(GameConfig.WINDOW_WIDTH);
        deathOverlay.setPrefHeight(GameConfig.WINDOW_HEIGHT);
        deathOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.65);");
        deathOverlay.setPickOnBounds(true);

        DropShadow redShadow = new DropShadow();
        redShadow.setColor(Color.web("#990000"));
        redShadow.setRadius(14);

        Label deathLabel = new Label(DEATH_LABEL_TEXT);
        deathLabel.setFont(AssetManager.getTitleFont());
        deathLabel.setTextFill(Color.web("#CC0000"));
        deathLabel.setEffect(redShadow);

        Label hintLabel = new Label(DEATH_HINT_TEXT);
        hintLabel.setFont(AssetManager.getTextFont());
        hintLabel.setTextFill(UIStyle.TEXT_COLOR);

        ProfessionalButton restartBtn = new ProfessionalButton("↺ RESTART");
        restartBtn.setOnAction(e -> { if (onRestartCallback != null) onRestartCallback.run(); });

        ProfessionalButton menuBtn = new ProfessionalButton("⌂ MENU");
        menuBtn.setOnAction(e -> { if (onMenuCallback != null) onMenuCallback.run(); });

        VBox content = new VBox(24, deathLabel, hintLabel, restartBtn, menuBtn);
        content.setAlignment(Pos.CENTER);
        deathOverlay.getChildren().add(content);

        getGameScene().addUINode(deathOverlay);
    }

    private Entity createScrollingBackground() {
        Image bgImage = AssetManager.loadImage(AssetManager.MENU_BG);
        if (bgImage == null) {
            return entityBuilder().at(0, 0).view(new Pane()).buildAndAttach();
        }

        double imgW = bgImage.getWidth();
        double imgH = bgImage.getHeight();
        double scale = Math.max((double) GameConfig.WINDOW_WIDTH / imgW, (double) GameConfig.WINDOW_HEIGHT / imgH);
        double fitW = imgW * scale;
        double fitH = imgH * scale;

        int tiles = (int) Math.ceil(GameConfig.WORLD_WIDTH / fitW) + 1;
        Pane bgContainer = new Pane();
        bgContainer.setPrefSize(GameConfig.WORLD_WIDTH, GameConfig.WINDOW_HEIGHT);

        for (int i = 0; i < tiles; i++) {
            ImageView bgView = new ImageView(bgImage);
            bgView.setPreserveRatio(true);
            bgView.setFitWidth(fitW);
            bgView.setSmooth(false);
            bgView.setTranslateX(i * fitW);
            bgView.setTranslateY((GameConfig.WINDOW_HEIGHT - fitH) / 2.0);
            bgContainer.getChildren().add(bgView);
        }

        return entityBuilder()
                .at(0, 0)
                .view(bgContainer)
                .buildAndAttach();
    }

    @Override
    public void update() {
        if (combatTextLabel != null && combatTextTimer > 0) {
            combatTextTimer = Math.max(0, combatTextTimer - 1.0 / 60.0);
            combatTextLabel.setVisible(combatTextTimer > 0);
        }
    }

    @Override
    public void cleanup() {
        removeUINode(hudRoot, "HUD root");
        removeUINode(deathOverlay, "death overlay");
        removeEntity(player, "player");
        removeEntity(ground, "ground");
        removeEntity(background, "background");
        for (Entity e : platforms) {
            removeEntity(e, "platform");
        }
        for (Entity e : obstacles) {
            removeEntity(e, "obstacle");
        }
        for (Entity e : enemies) {
            removeEntity(e, "enemy");
        }
        for (Entity e : pickups) {
            removeEntity(e, "pickup");
        }
        for (Entity e : new ArrayList<>(getGameWorld().getEntitiesByType(org.example.entity.EntityType.PLAYER_ARROW))) {
            removeEntity(e, "player arrow");
        }
        platforms.clear();
        obstacles.clear();
        enemies.clear();
        pickups.clear();
        player = null;
        playerComponent = null;
        hudRoot = null;
        deathOverlay = null;
    }

    public PlayerComponent getPlayerComponent() {
        return playerComponent;
    }

    private HBox createPlayerHealthHud() {
        HBox healthBox = new HBox(8);
        healthBox.setAlignment(Pos.CENTER_LEFT);
        healthBox.setPadding(new Insets(16, 0, 0, 14));
        healthBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        healthBox.setPickOnBounds(false);

        playerHealthLabel = new Label("HP");
        playerHealthLabel.setFont(AssetManager.getSmallFont());
        playerHealthLabel.setTextFill(UIStyle.TEXT_COLOR);

        weaponLabel = new Label("Weapon: Sword");
        weaponLabel.setFont(AssetManager.getSmallFont());
        weaponLabel.setTextFill(UIStyle.TEXT_COLOR);

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(AssetManager.getSmallFont());
        scoreLabel.setTextFill(UIStyle.TEXT_COLOR);

        Pane bar = new Pane();
        bar.setPrefSize(160, 14);

        Rectangle back = new Rectangle(160, 14);
        back.setFill(Color.web("#2C1810"));
        back.setStroke(UIStyle.ACCENT_COLOR);
        back.setStrokeWidth(1.5);

        playerHealthFill = new Rectangle(157, 11);
        playerHealthFill.setTranslateX(1.5);
        playerHealthFill.setTranslateY(1.5);
        playerHealthFill.setFill(Color.web("#4FBF5F"));

        bar.getChildren().addAll(back, playerHealthFill);
        healthBox.getChildren().addAll(playerHealthLabel, bar, weaponLabel, scoreLabel);
        return healthBox;
    }

    private void updatePlayerHealthHud(int health) {
        if (playerHealthFill == null || playerHealthLabel == null) {
            return;
        }

        double healthPercent = (double) health / GameConfig.PLAYER_MAX_HEALTH;
        playerHealthFill.setWidth(157 * healthPercent);
        playerHealthLabel.setText("HP " + health);

        if (healthPercent <= 0.3) {
            playerHealthFill.setFill(Color.web("#D94A38"));
        } else if (healthPercent <= 0.6) {
            playerHealthFill.setFill(Color.web("#E0A82E"));
        } else {
            playerHealthFill.setFill(Color.web("#4FBF5F"));
        }
    }

    private void updateWeaponHud(String weaponName) {
        if (weaponLabel != null) {
            weaponLabel.setText("Weapon: " + weaponName);
        }
    }

    private void addScore(int amount) {
        score += amount;
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
    }

    private Label createCombatTextLabel() {
        Label label = new Label();
        label.setFont(AssetManager.getHeadingFont());
        label.setTextFill(Color.web("#FFE066"));
        label.setEffect(TITLE_SHADOW);
        label.setVisible(false);
        label.setMouseTransparent(true);
        return label;
    }

    private void showCombatText(String text) {
        if (combatTextLabel == null) {
            return;
        }
        combatTextLabel.setText(text);
        combatTextLabel.setVisible(true);
        combatTextTimer = 0.35;
    }

    private static DropShadow createShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(3);
        shadow.setOffsetY(3);
        shadow.setRadius(4);
        shadow.setColor(Color.web("#1C0F08"));
        return shadow;
    }

    private void removeUINode(javafx.scene.Node node, String label) {
        if (node == null) {
            return;
        }
        try {
            getGameScene().removeUINode(node);
        } catch (RuntimeException e) {
            System.err.println("Could not remove " + label + " UI node: " + e.getMessage());
        }
    }

    private void removeEntity(Entity entity, String label) {
        if (entity == null) {
            return;
        }
        try {
            entity.removeFromWorld();
        } catch (RuntimeException e) {
            System.err.println("Could not remove " + label + " entity: " + e.getMessage());
        }
    }
}
