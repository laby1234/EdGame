package org.example.screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.config.GameConfig;
import org.example.ui.AssetManager;
import org.example.ui.ProfessionalButton;
import org.example.ui.UIStyle;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;

public class MenuScreen extends Screen {

    private static final DropShadow TITLE_SHADOW = createShadow();

    private StackPane rootPane;
    private Runnable onStartCallback;
    private Runnable onExitCallback;
    private Runnable onSettingsCallback;

    public MenuScreen(Runnable onStartCallback, Runnable onExitCallback, Runnable onSettingsCallback) {
        this.onStartCallback = onStartCallback;
        this.onExitCallback = onExitCallback;
        this.onSettingsCallback = onSettingsCallback;
    }

    @Override
    public void init() {
        rootPane = new StackPane();
        rootPane.setPrefWidth(GameConfig.WINDOW_WIDTH);
        rootPane.setPrefHeight(GameConfig.WINDOW_HEIGHT);

        Image bgImage = AssetManager.loadImage(AssetManager.MENU_BG);
        if (bgImage != null) {
            ImageView bgView = new ImageView(bgImage);
            double imgW = bgImage.getWidth();
            double imgH = bgImage.getHeight();
            double scale = Math.max((double) GameConfig.WINDOW_WIDTH / imgW, (double) GameConfig.WINDOW_HEIGHT / imgH);
            double fitW = imgW * scale;
            double fitH = imgH * scale;

            bgView.setPreserveRatio(true);
            bgView.setFitWidth(fitW);
            bgView.setSmooth(false);

            bgView.setTranslateX((GameConfig.WINDOW_WIDTH - fitW) / 2.0);
            bgView.setTranslateY((GameConfig.WINDOW_HEIGHT - fitH) / 2.0);

            javafx.scene.layout.Pane bgContainer = new javafx.scene.layout.Pane(bgView);
            bgContainer.setPrefWidth(GameConfig.WINDOW_WIDTH);
            bgContainer.setPrefHeight(GameConfig.WINDOW_HEIGHT);
            bgContainer.setMaxSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
            bgContainer.setClip(clip);

            rootPane.getChildren().add(bgContainer);
        } else {
            rootPane.setStyle(UIStyle.GRADIENT_BG);
        }

        rootPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
            }
        });
        rootPane.setFocusTraversable(true);

        VBox contentPane = createContentPane();
        rootPane.getChildren().add(contentPane);
        StackPane.setAlignment(contentPane, Pos.CENTER);
        rootPane.requestFocus();

        getGameScene().addUINode(rootPane);
    }

    private VBox createContentPane() {
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(50));
        mainContainer.setFillWidth(true);
        mainContainer.setMaxWidth(GameConfig.WINDOW_WIDTH);
        mainContainer.setStyle("-fx-background-color: transparent;");

        Label titleLabel = new Label("Ed Adventure");
        titleLabel.setFont(AssetManager.getTitleFont());
        titleLabel.setTextFill(UIStyle.ACCENT_COLOR);
        titleLabel.setEffect(TITLE_SHADOW);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(0));
        buttonContainer.setFillWidth(true);

        ProfessionalButton startBtn = new ProfessionalButton("▶ START GAME");
        startBtn.getButton().setFont(AssetManager.getTextFont());
        startBtn.setOnAction(e -> {
            if (onStartCallback != null) {
                onStartCallback.run();
            }
        });

        ProfessionalButton settingsBtn = new ProfessionalButton("⚙ SETTINGS");
        settingsBtn.getButton().setFont(AssetManager.getTextFont());
        settingsBtn.setOnAction(e -> {
            if (onSettingsCallback != null) {
                cleanup();
                onSettingsCallback.run();
            }
        });

        ProfessionalButton exitBtn = new ProfessionalButton("✕ EXIT");
        exitBtn.getButton().setFont(AssetManager.getTextFont());
        exitBtn.setOnAction(e -> {
            if (onExitCallback != null) {
                onExitCallback.run();
            }
        });

        buttonContainer.getChildren().addAll(startBtn, settingsBtn, exitBtn);

        mainContainer.getChildren().addAll(titleLabel, buttonContainer);

        return mainContainer;
    }

    @Override
    public void update() {
    }

    @Override
    public void cleanup() {
        if (rootPane != null) {
            getGameScene().removeUINode(rootPane);
        }
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
