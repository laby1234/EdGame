package org.example;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import org.example.config.GameConfig;
import org.example.screen.GameScreen;
import org.example.screen.MenuScreen;
import org.example.state.GameState;

import static com.almasb.fxgl.dsl.FXGL.getGameController;

public class EdGameApplication extends GameApplication {

    private GameState currentState;
    private MenuScreen menuScreen;
    private GameScreen gameScreen;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(GameConfig.WINDOW_WIDTH);
        settings.setHeight(GameConfig.WINDOW_HEIGHT);
        settings.setTitle(GameConfig.WINDOW_TITLE);
    }

    @Override
    protected void initInput() {
        // Input is handled by screens
    }

    @Override
    protected void initGame() {
        currentState = GameState.MENU;
        showMenuScreen();
    }

    private void showMenuScreen() {
        menuScreen = new MenuScreen(
                this::startGame,      // onStartCallback
                this::exitGame        // onExitCallback
        );
        menuScreen.init();
    }

    private void startGame() {
        if (menuScreen != null) {
            menuScreen.cleanup();
        }
        currentState = GameState.PLAYING;
        gameScreen = new GameScreen();
        gameScreen.init();
    }

    private void exitGame() {
        getGameController().exit();
    }
}
