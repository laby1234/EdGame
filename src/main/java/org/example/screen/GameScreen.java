package org.example.screen;

import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
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
import org.example.entity.EntityType;
import org.example.entity.player.PlayerComponent;
import org.example.ui.AssetManager;
import org.example.ui.ProfessionalButton;
import org.example.ui.UIStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;

public class GameScreen extends Screen {

    private enum LevelType {
        FOREST,
        CAVE
    }

    private static final String DEATH_LABEL_TEXT = "YOU DIED";
    private static final String DEATH_HINT_TEXT = "Press R to restart";
    private static final DropShadow TITLE_SHADOW = createShadow();
    private static final double HUD_SCALE = 0.4;
    private static final double DAMAGE_TEXT_LIFETIME = 0.45;
    private static final double DAMAGE_TEXT_RISE_SPEED = 24.0;
    public static final String BOW = "Bow";

    private Entity player;
    private PlayerComponent playerComponent;
    private Entity ground;
    private Entity background;
    private Entity portal;
    private Entity chest;

    private StackPane hudRoot;
    private StackPane deathOverlay;
    private Rectangle playerHealthFill;
    private Label playerHealthLabel;
    private Label playerNameLabel;
    private ImageView weaponIcon;
    private Image weaponSwordImage;
    private Image weaponBowImage;
    private Label scoreLabel;
    private Label levelTitleLabel;

    private int score = 0;
    private boolean playerDead = false;
    private boolean chestOpened = false;
    private LevelType currentLevel = LevelType.FOREST;

    private final List<Entity> platforms = new ArrayList<>();
    private final List<Entity> obstacles = new ArrayList<>();
    private final List<Entity> enemies = new ArrayList<>();
    private final List<Entity> pickups = new ArrayList<>();
    private final List<DamageTextEntry> damageTexts = new ArrayList<>();

    private final Runnable onRestartCallback;
    private final Runnable onMenuCallback;
    private final Runnable onGameOverCallback;
    private final Runnable onVictoryCallback;

    public GameScreen(Runnable onRestartCallback, Runnable onMenuCallback, Runnable onGameOverCallback, Runnable onVictoryCallback) {
        this.onRestartCallback = onRestartCallback;
        this.onMenuCallback = onMenuCallback;
        this.onGameOverCallback = onGameOverCallback;
        this.onVictoryCallback = onVictoryCallback;
    }

    @Override
    public void init() {
        getGameScene().setBackgroundColor(Color.WHITE);
        createHud();

        player = EntityFactory.createPlayer();
        playerComponent = EntityFactory.getPlayerComponent(player);
        playerComponent.setOnDeath(this::onPlayerDied);
        playerComponent.setOnHealthChanged(this::updatePlayerHealthHud);
        playerComponent.setOnWeaponChanged(this::updateWeaponHud);

        loadLevel(LevelType.FOREST, true);

        getGameScene().getViewport().setBounds(0, 0, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        getGameScene().getViewport().bindToEntity(player, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.WINDOW_HEIGHT / 2.0);
    }

    private void createHud() {
        hudRoot = new StackPane();
        hudRoot.setPickOnBounds(false);
        hudRoot.setPrefWidth(GameConfig.WINDOW_WIDTH);
        hudRoot.setPrefHeight(GameConfig.WINDOW_HEIGHT);

        levelTitleLabel = new Label("Forest");
        levelTitleLabel.setFont(AssetManager.getTitleFont());
        levelTitleLabel.setTextFill(UIStyle.ACCENT_COLOR);
        levelTitleLabel.setEffect(TITLE_SHADOW);
        levelTitleLabel.setPadding(new Insets(14, 0, 0, 0));
        StackPane.setAlignment(levelTitleLabel, Pos.TOP_CENTER);

        Label hintLabel = new Label("LMB - use | Scroll - weapon | R - restart");
        hintLabel.setFont(AssetManager.getSmallFont());
        hintLabel.setTextFill(UIStyle.TEXT_COLOR);
        hintLabel.setPadding(new Insets(16, 14, 0, 0));
        StackPane.setAlignment(hintLabel, Pos.TOP_RIGHT);

        HBox healthBox = createPlayerHealthHud();
        StackPane.setAlignment(healthBox, Pos.TOP_LEFT);

        hudRoot.getChildren().addAll(levelTitleLabel, hintLabel, healthBox);
        getGameScene().addUINode(hudRoot);
    }

    private void loadLevel(LevelType level, boolean firstLoad) {
        clearLevelEntities();

        currentLevel = level;
        chestOpened = false;
        playerDead = false;
        updateLevelTitle();

        background = createScrollingBackground(level == LevelType.FOREST ? AssetManager.MENU_BG : AssetManager.CAVE_BG);
        ground = (level == LevelType.FOREST ? EntityFactory.createGround() : EntityFactory.createCaveGround());

        if (level == LevelType.FOREST) {
            spawnForestLayout();
            spawnForestEnemies();
            spawnForestPickups();
            portal = EntityFactory.createPortal(GameConfig.WORLD_WIDTH - 130, GameConfig.GROUND_Y - GameConfig.PORTAL_SIZE);
        } else {
            spawnCaveLayout();
            spawnCaveEnemies();
            spawnCavePickups();
            chest = EntityFactory.createChest(GameConfig.WORLD_WIDTH - 140, GameConfig.GROUND_Y - GameConfig.CHEST_HEIGHT, false);
        }

        if (firstLoad) {
            player.setX(GameConfig.PLAYER_START_X);
            player.setY(GameConfig.PLAYER_START_Y);
        } else {
            player.setX(GameConfig.PLAYER_START_X);
            player.setY(GameConfig.GROUND_Y - GameConfig.PLAYER_SIZE);
        }
    }

    private void updateLevelTitle() {
        if (levelTitleLabel != null) {
            levelTitleLabel.setText(currentLevel == LevelType.FOREST ? "Forest" : "Cave");
        }
    }

    private void spawnForestLayout() {
        platforms.add(EntityFactory.createPlatform(350, 480, 4));
        platforms.add(EntityFactory.createPlatform(600, 400, 3));
        platforms.add(EntityFactory.createPlatform(880, 460, 4));
        platforms.add(EntityFactory.createPlatform(1100, 360, 3));
        platforms.add(EntityFactory.createPlatform(1380, 440, 5));
        platforms.add(EntityFactory.createPlatform(1700, 380, 3));
        platforms.add(EntityFactory.createPlatform(1980, 450, 4));
        platforms.add(EntityFactory.createPlatform(2280, 350, 3));
        platforms.add(EntityFactory.createPlatform(2560, 420, 4));
        platforms.add(EntityFactory.createPlatform(2860, 460, 3));

        int groundObstacleY = GameConfig.GROUND_Y - GameConfig.TILE_SIZE;
        obstacles.add(EntityFactory.createObstacle(230, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(750, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(1250, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(1600, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(2150, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(2700, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(3100, groundObstacleY));

        obstacles.add(EntityFactory.createObstacle(1420, 440 - GameConfig.TILE_SIZE));
        obstacles.add(EntityFactory.createObstacle(2060, 450 - GameConfig.TILE_SIZE));
    }

    private void spawnCaveLayout() {
        platforms.add(EntityFactory.createStonePlatform(420, 500, 5));
        platforms.add(EntityFactory.createStonePlatform(820, 420, 4));
        platforms.add(EntityFactory.createStonePlatform(1200, 340, 3));
        platforms.add(EntityFactory.createStonePlatform(1540, 420, 5));
        platforms.add(EntityFactory.createStonePlatform(1940, 330, 3));
        platforms.add(EntityFactory.createStonePlatform(2280, 430, 4));
        platforms.add(EntityFactory.createStonePlatform(2660, 360, 5));
        platforms.add(EntityFactory.createStonePlatform(3160, 300, 3));

        int groundObstacleY = GameConfig.GROUND_Y - GameConfig.TILE_SIZE;
        obstacles.add(EntityFactory.createObstacle(520, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(960, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(1460, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(2080, groundObstacleY));
        obstacles.add(EntityFactory.createObstacle(2480, groundObstacleY));

        obstacles.add(EntityFactory.createObstacle(1230, 340 - GameConfig.TILE_SIZE));
        obstacles.add(EntityFactory.createObstacle(2310, 430 - GameConfig.TILE_SIZE));
    }

    private void spawnForestEnemies() {
        int groundEnemyY = GameConfig.GROUND_Y - GameConfig.ENEMY_HEIGHT;
        enemies.add(EntityFactory.createEnemy(520, groundEnemyY, 120, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(520, groundEnemyY)));
        enemies.add(EntityFactory.createEnemy(980, groundEnemyY, 140, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(980, groundEnemyY)));
        enemies.add(EntityFactory.createEnemy(1480, groundEnemyY, 160, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(1480, groundEnemyY)));
        enemies.add(EntityFactory.createEnemy(2360, groundEnemyY, 160, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(2360, groundEnemyY)));
        enemies.add(EntityFactory.createEnemy(2880, groundEnemyY, 130, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(2880, groundEnemyY)));
        enemies.add(EntityFactory.createEnemy(1480, 440 - GameConfig.ENEMY_HEIGHT, 1464, 1530, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(1480, 440 - GameConfig.ENEMY_HEIGHT)));
        enemies.add(EntityFactory.createEnemy(2110, 450 - GameConfig.ENEMY_HEIGHT, 1986, 2020, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(2110, 450 - GameConfig.ENEMY_HEIGHT)));
    }

    private void spawnCaveEnemies() {
        int groundEnemyY = GameConfig.GROUND_Y - GameConfig.ENEMY_HEIGHT;
        enemies.add(EntityFactory.createCaveEnemy(640, groundEnemyY, 520, 860, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(640, groundEnemyY)));
        enemies.add(EntityFactory.createCaveEnemy(1320, groundEnemyY, 1180, 1520, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(1320, groundEnemyY)));
        enemies.add(EntityFactory.createCaveEnemy(1760, 420 - GameConfig.ENEMY_HEIGHT, 1560, 1700, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(1760, 420 - GameConfig.ENEMY_HEIGHT)));
        enemies.add(EntityFactory.createCaveEnemy(2400, groundEnemyY, 2240, 2580, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(2400, groundEnemyY)));
        enemies.add(EntityFactory.createCaveEnemy(2940, 360 - GameConfig.ENEMY_HEIGHT, 2680, 2820, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(2940, 360 - GameConfig.ENEMY_HEIGHT)));
        enemies.add(EntityFactory.createCaveEnemy(3400, groundEnemyY, 3260, GameConfig.WORLD_WIDTH - 180, player, playerComponent, this::addScore, this::showDamageText, () -> tryDropHeart(3400, groundEnemyY)));
    }

    private void spawnForestPickups() {
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
    }

    private void spawnCavePickups() {
        pickups.add(EntityFactory.createCoin(470, 460, this::addScore));
        pickups.add(EntityFactory.createCoin(860, 380, this::addScore));
        pickups.add(EntityFactory.createCoin(1210, 300, this::addScore));
        pickups.add(EntityFactory.createCoin(1600, 380, this::addScore));
        pickups.add(EntityFactory.createCoin(1950, 290, this::addScore));
        pickups.add(EntityFactory.createCoin(2330, 390, this::addScore));
        pickups.add(EntityFactory.createCoin(2700, 320, this::addScore));
        pickups.add(EntityFactory.createCoin(3180, 260, this::addScore));
    }

    private void tryDropHeart(double x, double y) {
        if (ThreadLocalRandom.current().nextDouble() <= GameConfig.HEART_DROP_CHANCE) {
            Entity heart = EntityFactory.createHeart(x + 10, y + 10);
            pickups.add(heart);
        }
    }

    private void onPlayerDied() {
        if (playerDead) {
            return;
        }
        playerDead = true;
        if (onGameOverCallback != null) {
            onGameOverCallback.run();
        }
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

        ProfessionalButton restartBtn = new ProfessionalButton("RESTART");
        restartBtn.setOnAction(e -> {
            if (onRestartCallback != null) {
                onRestartCallback.run();
            }
        });

        ProfessionalButton menuBtn = new ProfessionalButton("MENU");
        menuBtn.setOnAction(e -> {
            if (onMenuCallback != null) {
                onMenuCallback.run();
            }
        });

        VBox content = new VBox(24, deathLabel, hintLabel, restartBtn, menuBtn);
        content.setAlignment(Pos.CENTER);
        deathOverlay.getChildren().add(content);

        getGameScene().addUINode(deathOverlay);
    }

    private Entity createScrollingBackground(String path) {
        Image bgImage = AssetManager.loadImage(path);
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
        double dt = 1.0 / 60.0;

        damageTexts.removeIf(entry -> {
            entry.timer -= dt;
            if (entry.timer <= 0) {
                removeEntity(entry.entity);
                return true;
            }
            entry.entity.translateY(-DAMAGE_TEXT_RISE_SPEED * dt);
            entry.entity.getViewComponent().setOpacity(Math.min(1.0, entry.timer / DAMAGE_TEXT_LIFETIME));
            return false;
        });

        if (player == null || playerComponent == null || playerComponent.isDead()) {
            return;
        }

        if (currentLevel == LevelType.FOREST && portal != null && intersects(player, portal)) {
            removeEntity(portal);
            portal = null;
            loadLevel(LevelType.CAVE, false);
            return;
        }

        if (currentLevel == LevelType.CAVE && chest != null && !chestOpened && intersects(player, chest)) {
            chestOpened = true;
            double x = chest.getX();
            double y = chest.getY();
            removeEntity(chest);
            chest = EntityFactory.createChest(x, y, true);

            if (onVictoryCallback != null) {
                onVictoryCallback.run();
            }
        }
    }

    @Override
    public void cleanup() {
        removeUINode(hudRoot);
        removeUINode(deathOverlay);

        removeEntity(player);
        removeEntity(ground);
        removeEntity(background);
        removeEntity(portal);
        removeEntity(chest);

        for (Entity e : new ArrayList<>(platforms)) {
            removeEntity(e);
        }
        for (Entity e : new ArrayList<>(obstacles)) {
            removeEntity(e);
        }
        for (Entity e : new ArrayList<>(enemies)) {
            removeEntity(e);
        }
        for (Entity e : new ArrayList<>(pickups)) {
            removeEntity(e);
        }
        for (Entity e : new ArrayList<>(getGameWorld().getEntitiesByType(EntityType.PLAYER_ARROW))) {
            removeEntity(e);
        }
        for (Entity e : new ArrayList<>(getGameWorld().getEntitiesByType(EntityType.ENEMY_PROJECTILE))) {
            removeEntity(e);
        }
        for (DamageTextEntry entry : new ArrayList<>(damageTexts)) {
            removeEntity(entry.entity);
        }

        platforms.clear();
        obstacles.clear();
        enemies.clear();
        pickups.clear();
        damageTexts.clear();

        player = null;
        playerComponent = null;
        ground = null;
        background = null;
        portal = null;
        chest = null;
        hudRoot = null;
        deathOverlay = null;
    }

    private void clearLevelEntities() {
        removeEntity(ground);
        removeEntity(background);
        removeEntity(portal);
        removeEntity(chest);

        for (Entity e : new ArrayList<>(platforms)) {
            removeEntity(e);
        }
        for (Entity e : new ArrayList<>(obstacles)) {
            removeEntity(e);
        }
        for (Entity e : new ArrayList<>(enemies)) {
            removeEntity(e);
        }
        for (Entity e : new ArrayList<>(pickups)) {
            removeEntity(e);
        }
        for (Entity e : new ArrayList<>(getGameWorld().getEntitiesByType(EntityType.ENEMY_PROJECTILE))) {
            removeEntity(e);
        }

        platforms.clear();
        obstacles.clear();
        enemies.clear();
        pickups.clear();

        ground = null;
        background = null;
        portal = null;
        chest = null;
    }

    public PlayerComponent getPlayerComponent() {
        return playerComponent;
    }

    private HBox createPlayerHealthHud() {
        HBox healthBox = new HBox(12);
        healthBox.setAlignment(Pos.CENTER_LEFT);
        healthBox.setPadding(new Insets(16, 0, 0, 14));
        healthBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        healthBox.setPickOnBounds(false);

        Image statusBg = AssetManager.loadImage(AssetManager.HUD_STATUS);
        Image weaponBg = AssetManager.loadImage(AssetManager.HUD_WEAPON);
        weaponSwordImage = AssetManager.loadImage("assets/textures/blocks/sword.png");
        weaponBowImage = AssetManager.loadImage("assets/textures/blocks/bow.png");

        StackPane statusHud = new StackPane();
        if (statusBg != null) {
            ImageView statusView = new ImageView(statusBg);
            statusView.setSmooth(false);
            statusView.setFitWidth(statusBg.getWidth() * HUD_SCALE);
            statusView.setFitHeight(statusBg.getHeight() * HUD_SCALE);
            statusHud.getChildren().add(statusView);
            statusHud.setPrefSize(statusBg.getWidth() * HUD_SCALE, statusBg.getHeight() * HUD_SCALE);
        } else {
            statusHud.setPrefSize(220 * HUD_SCALE, 64 * HUD_SCALE);
        }

        Pane statusOverlay = new Pane();
        statusOverlay.setPrefSize(statusHud.getPrefWidth(), statusHud.getPrefHeight());

        Image playerHeadImage = AssetManager.loadImage("assets/textures/sprites/player.png");
        ImageView playerHead = new ImageView(playerHeadImage);
        if (playerHeadImage != null) {
            playerHead.setFitWidth(28);
            playerHead.setFitHeight(28);
            playerHead.setPreserveRatio(true);
            playerHead.setSmooth(false);
        }
        playerHead.setTranslateX(25);
        playerHead.setTranslateY(25);

        playerNameLabel = new Label("Ed");
        playerNameLabel.setFont(AssetManager.getSmallFont());
        playerNameLabel.setTextFill(UIStyle.TEXT_COLOR);
        playerNameLabel.setTranslateX(320 * HUD_SCALE);
        playerNameLabel.setTranslateY(123 * HUD_SCALE);

        Pane bar = new Pane();
        double barW = 127;
        double barH = 14;
        bar.setPrefSize(barW, barH);
        bar.setTranslateX(75);
        bar.setTranslateY(19);

        Rectangle back = new Rectangle(barW, barH);
        back.setFill(Color.web("#2C1810"));
        back.setStroke(UIStyle.ACCENT_COLOR);
        back.setStrokeWidth(1.2);

        playerHealthFill = new Rectangle(125, 13);
        playerHealthFill.setTranslateX(1);
        playerHealthFill.setTranslateY(1);
        playerHealthFill.setFill(Color.web("#4FBF5F"));

        bar.getChildren().addAll(back, playerHealthFill);

        playerHealthLabel = new Label("HP 100");
        playerHealthLabel.setFont(AssetManager.getSmallFont());
        playerHealthLabel.setTextFill(UIStyle.TEXT_COLOR);
        playerHealthLabel.setTranslateX(120);
        playerHealthLabel.setTranslateY(17);

        statusOverlay.getChildren().addAll(playerHead, playerNameLabel, bar, playerHealthLabel);
        statusHud.getChildren().add(statusOverlay);

        StackPane weaponHud = new StackPane();
        if (weaponBg != null) {
            ImageView weaponBgView = new ImageView(weaponBg);
            weaponBgView.setSmooth(false);
            weaponBgView.setFitWidth(weaponBg.getWidth() * HUD_SCALE);
            weaponBgView.setFitHeight(weaponBg.getHeight() * HUD_SCALE);
            weaponHud.getChildren().add(weaponBgView);
            weaponHud.setPrefSize(weaponBg.getWidth() * HUD_SCALE, weaponBg.getHeight() * HUD_SCALE);
        } else {
            weaponHud.setPrefSize(64 * HUD_SCALE, 64 * HUD_SCALE);
        }

        weaponIcon = new ImageView(weaponSwordImage);
        weaponIcon.setFitWidth(45);
        weaponIcon.setFitHeight(45);
        weaponIcon.setPreserveRatio(true);
        weaponIcon.setSmooth(false);
        weaponHud.getChildren().add(weaponIcon);

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(AssetManager.getSmallFont());
        scoreLabel.setTextFill(UIStyle.TEXT_COLOR);

        healthBox.getChildren().addAll(statusHud, weaponHud, scoreLabel);
        return healthBox;
    }

    private void updatePlayerHealthHud(int health) {
        if (playerHealthFill == null || playerHealthLabel == null) {
            return;
        }

        double healthPercent = (double) health / GameConfig.PLAYER_MAX_HEALTH;
        playerHealthFill.setWidth(125 * healthPercent);
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
        if (weaponIcon == null) {
            return;
        }

        if (BOW.equalsIgnoreCase(weaponName)) {
            weaponIcon.setImage(weaponBowImage != null ? weaponBowImage : weaponIcon.getImage());
        } else {
            weaponIcon.setImage(weaponSwordImage != null ? weaponSwordImage : weaponIcon.getImage());
        }
    }

    private void addScore(int amount) {
        score += amount;
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
    }

    private void showDamageText(int damage, Entity enemy) {
        if (enemy == null) {
            return;
        }

        Label label = new Label("-" + damage);
        label.setFont(AssetManager.getHeadingFont());
        label.setTextFill(Color.web("#FFE066"));
        label.setEffect(TITLE_SHADOW);
        label.setMouseTransparent(true);

        double x = enemy.getX() + enemy.getWidth() * 0.5;
        double y = enemy.getY() - 12;
        Entity textEntity = entityBuilder().at(x, y).view(label).buildAndAttach();
        label.setTranslateX(-label.prefWidth(-1) / 2.0);
        damageTexts.add(new DamageTextEntry(textEntity, DAMAGE_TEXT_LIFETIME));
    }

    private boolean intersects(Entity a, Entity b) {
        double aLeft = a.getX();
        double aRight = aLeft + a.getWidth();
        double aTop = a.getY();
        double aBottom = aTop + a.getHeight();

        double bLeft = b.getX();
        double bRight = bLeft + b.getWidth();
        double bTop = b.getY();
        double bBottom = bTop + b.getHeight();

        return aRight > bLeft && aLeft < bRight && aBottom > bTop && aTop < bBottom;
    }

    private static class DamageTextEntry {
        private final Entity entity;
        private double timer;

        private DamageTextEntry(Entity entity, double timer) {
            this.entity = entity;
            this.timer = timer;
        }
    }

    private static DropShadow createShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(3);
        shadow.setOffsetY(3);
        shadow.setRadius(4);
        shadow.setColor(Color.web("#1C0F08"));
        return shadow;
    }

    private void removeEntity(Entity entity) {
        if (entity == null) {
            return;
        }
        if (entity.getWorld() != null) {
            getGameWorld().removeEntity(entity);
        }
    }

    private void removeUINode(Node node) {
        if (node == null) {
            return;
        }
        getGameScene().removeUINode(node);
    }
}