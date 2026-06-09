package org.example.screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.audio.AudioManager;
import org.example.config.GameConfig;
import org.example.ui.AssetManager;
import org.example.ui.ProfessionalButton;
import org.example.ui.UIStyle;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGLForKtKt.play;

public class CreditsScreen extends Screen {

    private static final DropShadow TITLE_SHADOW = createShadow(3, 3, 4, Color.web("#1C0F08"));
    private static final DropShadow TEXT_SHADOW = createShadow(1, 1, 2, Color.web("#1C0F08"));

    private final Runnable onBackToMenu;
    private StackPane rootPane;

    public CreditsScreen(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;
    }

    @Override
    public void init() {
        rootPane = new StackPane();
        rootPane.setPrefWidth(GameConfig.WINDOW_WIDTH);
        rootPane.setPrefHeight(GameConfig.WINDOW_HEIGHT);
        rootPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.68);");
        AudioManager.playSound(AssetManager.SFX_VICTORY);
        Image bgImage = AssetManager.loadImage(AssetManager.CAVE_BG);
        if (bgImage != null) {
            ImageView bgView = new ImageView(bgImage);
            bgView.setFitWidth(GameConfig.WINDOW_WIDTH);
            bgView.setFitHeight(GameConfig.WINDOW_HEIGHT);
            bgView.setPreserveRatio(false);
            bgView.setSmooth(false);
            rootPane.getChildren().add(bgView);
        } else {
            rootPane.setStyle(UIStyle.DARK_GRADIENT_BG);
        }

        StackPane overlay = new StackPane();
        overlay.setPrefWidth(GameConfig.WINDOW_WIDTH);
        overlay.setPrefHeight(GameConfig.WINDOW_HEIGHT);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");

        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setMaxWidth(520);
        content.setStyle(
                "-fx-background-color: rgba(20, 12, 8, 0.72);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(212, 176, 106, 0.45);" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 12;"
        );

        Label title = new Label("THE END");
        title.setFont(AssetManager.getTitleFont());
        title.setTextFill(UIStyle.ACCENT_COLOR);
        title.setEffect(TITLE_SHADOW);

        Label line1 = new Label("You recovered the treasure.");
        line1.setFont(AssetManager.getHeadingFont());
        line1.setTextFill(UIStyle.TEXT_COLOR);
        line1.setEffect(TEXT_SHADOW);

        Label line2 = new Label("Game by: Michal Janus, Tomasz Jachowicz");
        line2.setFont(AssetManager.getTextFont());
        line2.setTextFill(UIStyle.TEXT_COLOR);
        line2.setEffect(TEXT_SHADOW);

        Label line3 = new Label("Thanks for playing!");
        line3.setFont(AssetManager.getTextFont());
        line3.setTextFill(UIStyle.TEXT_COLOR);
        line3.setEffect(TEXT_SHADOW);

        ProfessionalButton menuBtn = new ProfessionalButton("MENU");
        menuBtn.setMinSize(280, 58);
        menuBtn.setOnAction(e -> {
            if (onBackToMenu != null) {
                cleanup();
                onBackToMenu.run();
            }
        });

        content.getChildren().addAll(title, line1, line2, line3, menuBtn);
        overlay.getChildren().add(content);
        rootPane.getChildren().add(overlay);

        getGameScene().addUINode(rootPane);
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

    private static DropShadow createShadow(double offsetX, double offsetY, double radius, Color color) {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(offsetX);
        shadow.setOffsetY(offsetY);
        shadow.setRadius(radius);
        shadow.setColor(color);
        return shadow;
    }
}