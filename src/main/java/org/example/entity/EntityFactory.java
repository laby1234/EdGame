package org.example.entity;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.config.GameConfig;
import org.example.entity.player.PlayerComponent;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.texture;

public class EntityFactory {

    public static Entity createGround() {
        int cols = (int) Math.ceil((double) GameConfig.WORLD_WIDTH / GameConfig.TILE_SIZE);
        int rows = (int) Math.ceil((double) GameConfig.GROUND_HEIGHT / GameConfig.TILE_SIZE);

        Pane groundView = new Pane();
        groundView.setPrefSize(cols * GameConfig.TILE_SIZE, rows * GameConfig.TILE_SIZE);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String tileName = row == 0 ? "blocks/grass.png" : "blocks/dirt.png";
                Texture tile = texture(tileName, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                tile.setTranslateX(col * GameConfig.TILE_SIZE);
                tile.setTranslateY(row * GameConfig.TILE_SIZE);
                groundView.getChildren().add(tile);
            }
        }

        return entityBuilder()
                .at(0, GameConfig.GROUND_Y)
                .view(groundView)
                .buildAndAttach();
    }

    public static Entity createPlayer() {
        PlayerComponent playerComponent = new PlayerComponent();
        StackPane container = new StackPane();
        container.setPrefSize(GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);
        container.getChildren().add(texture("sprites/player_idle1.png", GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE));
        playerComponent.setTextureContainer(container);

        return entityBuilder()
                .at(GameConfig.PLAYER_START_X, GameConfig.PLAYER_START_Y)
                .view(container)
                .with(playerComponent)
                .buildAndAttach();
    }

    public static PlayerComponent getPlayerComponent(Entity player) {
        return player.getComponent(PlayerComponent.class);
    }
}
