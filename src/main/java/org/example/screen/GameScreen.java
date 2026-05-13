package org.example.screen;

import com.almasb.fxgl.entity.Entity;
import javafx.scene.paint.Color;
import org.example.entity.EntityFactory;
import org.example.entity.player.PlayerComponent;
import org.example.input.InputManager;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;

public class GameScreen extends Screen {

    private Entity player;
    private PlayerComponent playerComponent;
    private InputManager inputManager;

    @Override
    public void init() {
        getGameScene().setBackgroundColor(Color.WHITE);

        // Create ground
        EntityFactory.createGround();

        // Create player
        player = EntityFactory.createPlayer();
        playerComponent = EntityFactory.getPlayerComponent(player);

        // Setup input
        inputManager = new InputManager(playerComponent, true);
        inputManager.setupInput();
    }

    @Override
    public void update() {
        // Per-frame updates if needed
    }

    @Override
    public void cleanup() {
        // Clean up game entities if needed
    }

    public PlayerComponent getPlayerComponent() {
        return playerComponent;
    }
}
