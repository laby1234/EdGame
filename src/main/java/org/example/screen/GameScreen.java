package org.example.screen;

import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

public class GameScreen extends Screen {

    private static final DropShadow TITLE_SHADOW = createShadow();

    private Entity player;
    private PlayerComponent playerComponent;
    private Entity ground;
    private Entity background;
    private StackPane hudRoot;
    private StackPane deathOverlay;

    private final List<Entity> platforms = new ArrayList<>();
    private final List<Entity> obstacles = new ArrayList<>();

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
        hintLabel.setFont(AssetManager.getSmallFont());
        hintLabel.setTextFill(UIStyle.TEXT_COLOR);
        hintLabel.setPadding(new Insets(16, 14, 0, 0));
        StackPane.setAlignment(hintLabel, Pos.TOP_RIGHT);

        hudRoot.getChildren().addAll(titleLabel, hintLabel);
        getGameScene().addUINode(hudRoot);

        background = createScrollingBackground();
        ground = EntityFactory.createGround();

        spawnPlatformsAndObstacles();

        player = EntityFactory.createPlayer();
        playerComponent = EntityFactory.getPlayerComponent(player);
        playerComponent.setOnDeath(this::onPlayerDied);

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

        Label deathLabel = new Label("YOU DIED");
        deathLabel.setFont(AssetManager.getTitleFont());
        deathLabel.setTextFill(Color.web("#CC0000"));
        deathLabel.setEffect(redShadow);

        Label hintLabel = new Label("Press R to restart");
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
    }

    @Override
    public void cleanup() {
        try { if (hudRoot != null)      getGameScene().removeUINode(hudRoot);      } catch (Exception ignored) {}
        try { if (deathOverlay != null) getGameScene().removeUINode(deathOverlay); } catch (Exception ignored) {}
        try { if (player != null)     player.removeFromWorld();     } catch (Exception ignored) {}
        try { if (ground != null)     ground.removeFromWorld();     } catch (Exception ignored) {}
        try { if (background != null) background.removeFromWorld(); } catch (Exception ignored) {}
        for (Entity e : platforms) { try { e.removeFromWorld(); } catch (Exception ignored) {} }
        for (Entity e : obstacles) { try { e.removeFromWorld(); } catch (Exception ignored) {} }
        platforms.clear();
        obstacles.clear();
        player = null;
        playerComponent = null;
        hudRoot = null;
        deathOverlay = null;
    }

    public PlayerComponent getPlayerComponent() {
        return playerComponent;
    }

    private static DropShadow createShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(3);
        shadow.setOffsetY(3);
        shadow.setRadius(4);
        shadow.setColor(Color.web("#1C0F08"));
        return shadow;
    }
}