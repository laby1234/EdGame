package org.example;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import org.example.config.GameConfig;
import org.example.screen.CreditsScreen;
import org.example.screen.GameScreen;
import org.example.screen.MenuScreen;
import org.example.screen.PauseScreen;
import org.example.screen.SettingsScreen;
import org.example.state.GameState;

import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.onKey;

public class EdGameApplication extends GameApplication {

    private GameState currentState;
    private MenuScreen menuScreen;
    private SettingsScreen settingsScreen;
    private PauseScreen pauseScreen;
    private GameScreen gameScreen;
    private CreditsScreen creditsScreen;

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

        getInput().addAction(new UserAction("Restart") {
            @Override
            protected void onActionBegin() {
                if (currentState == GameState.PLAYING || currentState == GameState.GAME_OVER || currentState == GameState.VICTORY) {
                    restartGame();
                }
            }
        }, KeyCode.R);

        getInput().addAction(new UserAction("MoveLeft") {
            @Override
            protected void onActionBegin() {
                if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().setMovingLeft(true);
                }
            }

            @Override
            protected void onActionEnd() {
                if (gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().setMovingLeft(false);
                }
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("MoveLeftArrow") {
            @Override
            protected void onActionBegin() {
                if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().setMovingLeft(true);
                }
            }

            @Override
            protected void onActionEnd() {
                if (gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().setMovingLeft(false);
                }
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("MoveRight") {
            @Override
            protected void onActionBegin() {
                if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().setMovingRight(true);
                }
            }

            @Override
            protected void onActionEnd() {
                if (gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().setMovingRight(false);
                }
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("MoveRightArrow") {
            @Override
            protected void onActionBegin() {
                if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().setMovingRight(true);
                }
            }

            @Override
            protected void onActionEnd() {
                if (gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().setMovingRight(false);
                }
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("JumpW") {
            @Override
            protected void onActionBegin() {
                if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().requestJump();
                }
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("JumpUp") {
            @Override
            protected void onActionBegin() {
                if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().requestJump();
                }
            }
        }, KeyCode.UP);

        getInput().addAction(new UserAction("JumpSpace") {
            @Override
            protected void onActionBegin() {
                if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().requestJump();
                }
            }
        }, KeyCode.SPACE);

        getInput().addAction(new UserAction("ScoreHeal") {
            @Override
            protected void onActionBegin() {
                if (currentState == GameState.PLAYING && gameScreen != null) {
                    gameScreen.tryBuyHeal();
                }
            }
        }, KeyCode.E);

        getInput().addEventHandler(ScrollEvent.SCROLL, event -> {
            if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                gameScreen.getPlayerComponent().switchWeaponByScroll(event.getDeltaY());
            }
        });

        getInput().addAction(new UserAction("UseWeapon") {
            @Override
            protected void onActionBegin() {
                if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().startWeaponAction();
                }
            }

            @Override
            protected void onActionEnd() {
                if (currentState == GameState.PLAYING && gameScreen != null && gameScreen.getPlayerComponent() != null) {
                    gameScreen.getPlayerComponent().releaseWeaponAction();
                }
            }
        }, MouseButton.PRIMARY);
    }

    @Override
    protected void initGame() {
        currentState = GameState.MENU;
        showMenuScreen();
    }

    @Override
    protected void onUpdate(double tpf) {
        if (currentState == GameState.PLAYING && gameScreen != null) {
            gameScreen.update(tpf);
        }
    }

    private void showMenuScreen() {
        cleanupAllScreens();
        currentState = GameState.MENU;
        menuScreen = new MenuScreen(this::startGame, this::exitGame, this::showSettingsScreen);
        menuScreen.init();
    }

    private void showSettingsScreen() {
        if (menuScreen != null) {
            menuScreen.cleanup();
            menuScreen = null;
        }
        settingsScreen = new SettingsScreen(this::showMenuScreen);
        settingsScreen.init();
    }

    private void startGame() {
        cleanupAllScreens();
        currentState = GameState.PLAYING;
        gameScreen = new GameScreen(true,this::restartGame, this::backToMenu, this::onGameOver, this::onVictory);
        gameScreen.init();
    }

    private void restartGame() {
        if (gameScreen != null) {
            gameScreen.cleanup();
            gameScreen = null;
        }
        if (creditsScreen != null) {
            creditsScreen.cleanup();
            creditsScreen = null;
        }
        currentState = GameState.PLAYING;
        gameScreen = new GameScreen(false,this::restartGame, this::backToMenu, this::onGameOver, this::onVictory);
        gameScreen.init();
    }

    private void onGameOver() {
        currentState = GameState.GAME_OVER;
    }

    private void onVictory() {
        currentState = GameState.VICTORY;
        int finalScore = 0;
        double finalTime = 0;

        if (gameScreen != null) {
            finalScore = gameScreen.getScore();
            finalTime = gameScreen.getElapsedTime();
            gameScreen.cleanup();
            gameScreen = null;
        }
        creditsScreen = new CreditsScreen(this::showMenuScreen, finalScore, finalTime);
        creditsScreen.init();
        currentState = GameState.CREDITS;
    }

    private void backToMenu() {
        cleanupAllScreens();
        currentState = GameState.MENU;
        showMenuScreen();
    }

    private void showPauseScreen() {
        pauseScreen = new PauseScreen(this::resumeGame, this::backToMenu);
        pauseScreen.init();
    }

    private void resumeGame() {
        if (pauseScreen != null) {
            pauseScreen.cleanup();
            pauseScreen = null;
        }
        currentState = GameState.PLAYING;
    }

    private void exitGame() {
        getGameController().exit();
    }

    private void cleanupAllScreens() {
        if (menuScreen != null) {
            menuScreen.cleanup();
            menuScreen = null;
        }
        if (settingsScreen != null) {
            settingsScreen.cleanup();
            settingsScreen = null;
        }
        if (pauseScreen != null) {
            pauseScreen.cleanup();
            pauseScreen = null;
        }
        if (gameScreen != null) {
            gameScreen.cleanup();
            gameScreen = null;
        }
        if (creditsScreen != null) {
            creditsScreen.cleanup();
            creditsScreen = null;
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }
}
