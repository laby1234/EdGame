package org.example;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.input.KeyCode;
import org.example.config.GameConfig;
import org.example.screen.GameScreen;
import org.example.screen.MenuScreen;
import org.example.screen.PauseScreen;
import org.example.screen.SettingsScreen;
import org.example.state.GameState;

import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.onKey;
import static com.almasb.fxgl.dsl.FXGL.onKeyDown;

public class EdGameApplication extends GameApplication {

    private GameState currentState;
    private MenuScreen menuScreen;
    private SettingsScreen settingsScreen;
    private PauseScreen pauseScreen;
    private GameScreen gameScreen;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(GameConfig.WINDOW_WIDTH);
        settings.setHeight(GameConfig.WINDOW_HEIGHT);
        settings.setTitle(GameConfig.WINDOW_TITLE);
        
        settings.setDeveloperMenuEnabled(false);
        settings.setMainMenuEnabled(false);
        settings.setGameMenuEnabled(false);
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.ESCAPE, () -> {
            if (currentState == GameState.PLAYING) {
                currentState = GameState.PAUSED;
                showPauseScreen();
            }
        });

        onKey(KeyCode.A, () -> {
            if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                gameScreen.getPlayerComponent().moveLeft();
            }
        });
        onKey(KeyCode.LEFT, () -> {
            if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                gameScreen.getPlayerComponent().moveLeft();
            }
        });
        onKey(KeyCode.D, () -> {
            if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                gameScreen.getPlayerComponent().moveRight();
            }
        });
        onKey(KeyCode.RIGHT, () -> {
            if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                gameScreen.getPlayerComponent().moveRight();
            }
        });

        onKeyDown(KeyCode.W, () -> {
            if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                gameScreen.getPlayerComponent().requestJump();
            }
        });
        onKeyDown(KeyCode.UP, () -> {
            if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                gameScreen.getPlayerComponent().requestJump();
            }
        });
        onKeyDown(KeyCode.SPACE, () -> {
            if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                gameScreen.getPlayerComponent().requestJump();
            }
        });
    }


    @Override
    protected void initGame() {
        currentState = GameState.MENU;
        showMenuScreen();
    }

    private void showMenuScreen() {
        menuScreen = new MenuScreen(
                this::startGame,          // onStartCallback
                this::exitGame,           // onExitCallback
                this::showSettingsScreen  // onSettingsCallback
        );
        menuScreen.init();
    }

    private void showSettingsScreen() {
        settingsScreen = new SettingsScreen(this::showMenuScreen);
        settingsScreen.init();
    }

    private void startGame() {
        if (menuScreen != null) {
            menuScreen.cleanup();
        }
        currentState = GameState.PLAYING;
        gameScreen = new GameScreen();
        gameScreen.init();
    }

    private void showPauseScreen() {
        pauseScreen = new PauseScreen(
                this::resumeGame,
                this::backToMenuFromPause
        );
        pauseScreen.init();
    }

    private void resumeGame() {
        if (pauseScreen != null) {
            pauseScreen.cleanup();
        }
        currentState = GameState.PLAYING;
    }

    private void backToMenuFromPause() {
        if (pauseScreen != null) {
            pauseScreen.cleanup();
        }
        if (gameScreen != null) {
            gameScreen.cleanup();
        }
        currentState = GameState.MENU;
        showMenuScreen();
    }

    private void exitGame() {
        getGameController().exit();
    }



    public GameState getCurrentState() {
        return currentState;
    }
}
