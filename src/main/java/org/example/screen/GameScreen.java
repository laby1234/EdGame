package org.example.screen;

import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.example.config.GameConfig;
import org.example.entity.EntityFactory;
import org.example.entity.player.PlayerComponent;
import org.example.input.InputManager;
import org.example.ui.AssetManager;
import org.example.ui.UIStyle;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;

public class GameScreen extends Screen {

    private static final DropShadow TITLE_SHADOW = createShadow();

    private Entity player;
    private PlayerComponent playerComponent;
    private Entity ground;
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

        Label titleLabel = new Label("EdGame");
        titleLabel.setFont(AssetManager.getTitleFont());
        titleLabel.setTextFill(UIStyle.ACCENT_COLOR);
        titleLabel.setEffect(TITLE_SHADOW);
        titleLabel.setPadding(new Insets(14, 0, 0, 0));
        StackPane.setAlignment(titleLabel, Pos.TOP_CENTER);
        hudRoot.getChildren().add(titleLabel);
        getGameScene().addUINode(hudRoot);

        ground = EntityFactory.createGround();

        player = EntityFactory.createPlayer();
        playerComponent = EntityFactory.getPlayerComponent(player);

        new InputManager(playerComponent, true);
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
