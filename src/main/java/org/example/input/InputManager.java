package org.example.input;

import javafx.scene.input.KeyCode;
import org.example.entity.player.PlayerComponent;

import static com.almasb.fxgl.dsl.FXGL.onKey;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;

public class InputManager {

    private PlayerComponent playerComponent;
    private boolean gameActive;

    public InputManager(PlayerComponent playerComponent, boolean gameActive) {
        this.playerComponent = playerComponent;
        this.gameActive = gameActive;
    }

    public void setupInput() {
        onKey(KeyCode.A, () -> {
            if (gameActive) playerComponent.moveLeft();
        });
        onKey(KeyCode.LEFT, () -> {
            if (gameActive) playerComponent.moveLeft();
        });
        onKey(KeyCode.D, () -> {
            if (gameActive) playerComponent.moveRight();
        });
        onKey(KeyCode.RIGHT, () -> {
            if (gameActive) playerComponent.moveRight();
        });

        onKeyDown(KeyCode.W, () -> {
            if (gameActive) playerComponent.requestJump();
        });
        onKeyDown(KeyCode.UP, () -> {
            if (gameActive) playerComponent.requestJump();
        });
        onKeyDown(KeyCode.SPACE, () -> {
            if (gameActive) playerComponent.requestJump();
        });
    }

    public void setGameActive(boolean active) {
        this.gameActive = active;
    }

    public void setPlayerComponent(PlayerComponent component) {
        this.playerComponent = component;
    }
}
