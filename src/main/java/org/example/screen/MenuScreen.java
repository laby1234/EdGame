package org.example.screen;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.config.GameConfig;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;

public class MenuScreen extends Screen {

    private StackPane menuPane;
    private Runnable onStartCallback;
    private Runnable onExitCallback;

    public MenuScreen(Runnable onStartCallback, Runnable onExitCallback) {
        this.onStartCallback = onStartCallback;
        this.onExitCallback = onExitCallback;
    }

    @Override
    public void init() {
        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setStyle("-fx-font-size: 18px;");

        Button startBtn = new Button("START");
        startBtn.setStyle("-fx-font-size: 16px; -fx-padding: 10px 40px;");
        startBtn.setOnAction(e -> {
            if (onStartCallback != null) {
                onStartCallback.run();
            }
        });

        Button exitBtn = new Button("EXIT");
        exitBtn.setStyle("-fx-font-size: 16px; -fx-padding: 10px 40px;");
        exitBtn.setOnAction(e -> {
            if (onExitCallback != null) {
                onExitCallback.run();
            }
        });

        menuBox.getChildren().addAll(startBtn, exitBtn);

        menuPane = new StackPane(menuBox);
        menuPane.setStyle("-fx-background-color: lightblue;");
        menuPane.setPrefWidth(GameConfig.WINDOW_WIDTH);
        menuPane.setPrefHeight(GameConfig.WINDOW_HEIGHT);

        getGameScene().addUINode(menuPane);
    }

    @Override
    public void update() {
        // Menu doesn't need per-frame updates
    }

    @Override
    public void cleanup() {
        if (menuPane != null) {
            getGameScene().removeUINode(menuPane);
        }
    }
}
