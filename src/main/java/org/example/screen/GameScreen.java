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
import javafx.scene.paint.Color;
import org.example.config.GameConfig;
import org.example.entity.EntityFactory;
import org.example.entity.player.PlayerComponent;
import org.example.ui.AssetManager;
import org.example.ui.UIStyle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;

public class GameScreen extends Screen {

    private static final DropShadow TITLE_SHADOW = createShadow();

    private Entity player;
    private PlayerComponent playerComponent;
    private Entity ground;
    private Entity background;
    private StackPane hudRoot;

    public GameScreen() {
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
        hudRoot.getChildren().add(titleLabel);
        getGameScene().addUINode(hudRoot);

        background = createScrollingBackground();

        ground = EntityFactory.createGround();

        player = EntityFactory.createPlayer();
        playerComponent = EntityFactory.getPlayerComponent(player);

        // Bind camera to player for side-scrolling
        getGameScene().getViewport().setBounds(0, 0, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        getGameScene().getViewport().bindToEntity(player, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.WINDOW_HEIGHT / 2.0);
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
        try {
            if (hudRoot != null) getGameScene().removeUINode(hudRoot);
        } catch (Exception ignored) {}
        try {
            if (player != null) player.removeFromWorld();
        } catch (Exception ignored) {}
        try {
            if (ground != null) ground.removeFromWorld();
        } catch (Exception ignored) {}
        try {
            if (background != null) background.removeFromWorld();
        } catch (Exception ignored) {}
        player = null;
        playerComponent = null;
        hudRoot = null;
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
