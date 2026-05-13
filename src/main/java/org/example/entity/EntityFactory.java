package org.example.entity;

import com.almasb.fxgl.entity.Entity;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.config.GameConfig;
import org.example.entity.player.PlayerComponent;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.texture;

public class EntityFactory {

    public static Entity createGround() {
        return entityBuilder()
                .at(0, GameConfig.GROUND_Y)
                .view(new Rectangle(GameConfig.WINDOW_WIDTH, GameConfig.GROUND_HEIGHT, Color.DARKGREEN))
                .buildAndAttach();
    }

    public static Entity createPlayer() {
        PlayerComponent playerComponent = new PlayerComponent();
        return entityBuilder()
                .at(GameConfig.PLAYER_START_X, GameConfig.PLAYER_START_Y)
                .view(texture("player.png", GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE))
                .with(playerComponent)
                .buildAndAttach();
    }

    public static PlayerComponent getPlayerComponent(Entity player) {
        return player.getComponent(PlayerComponent.class);
    }
}

