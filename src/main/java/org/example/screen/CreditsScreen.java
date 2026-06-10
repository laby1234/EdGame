package org.example.screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.audio.AudioManager;
import org.example.config.GameConfig;
import org.example.leaderboard.LeaderboardEntry;
import org.example.leaderboard.LeaderboardStore;
import org.example.ui.AssetManager;
import org.example.ui.ProfessionalButton;
import org.example.ui.UIStyle;

import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;

public class CreditsScreen extends Screen {

    private static final DropShadow TITLE_SHADOW = createShadow(3, 3, 4, Color.web("#1C0F08"));
    private static final DropShadow TEXT_SHADOW = createShadow(1, 1, 2, Color.web("#1C0F08"));

    private final Runnable onBackToMenu;
    private final int finalScore;
    private final double finalTime;
    private StackPane rootPane;
    private VBox leaderboardBox;
    private TextField nameField;
    private ProfessionalButton saveBtn;

    public CreditsScreen(Runnable onBackToMenu, int finalScore, double finalTime) {
        this.onBackToMenu = onBackToMenu;
        this.finalScore = finalScore;
        this.finalTime = finalTime;
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

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));
        content.setMaxWidth(640);
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

        Label resultLabel = new Label("Score: " + finalScore + "   Time: " + new LeaderboardEntry("", finalScore, finalTime).formattedTime());
        resultLabel.setFont(AssetManager.getTextFont());
        resultLabel.setTextFill(UIStyle.ACCENT_COLOR);
        resultLabel.setEffect(TEXT_SHADOW);

        nameField = new TextField();
        nameField.setPromptText("Nickname");
        nameField.setMaxWidth(280);
        nameField.setFont(AssetManager.getSmallFont());
        nameField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.9);" +
                        "-fx-text-fill: #1C0F08;" +
                        "-fx-prompt-text-fill: #6B5A43;"
        );

        saveBtn = new ProfessionalButton("SAVE SCORE");
        saveBtn.setMinSize(220, 44);
        saveBtn.setOnAction(e -> saveScore());

        leaderboardBox = new VBox(6);
        leaderboardBox.setAlignment(Pos.CENTER_LEFT);
        leaderboardBox.setMaxWidth(500);
        renderLeaderboard(LeaderboardStore.loadTopEntries());

        Label line2 = new Label("Game by: Michal Janus, Tomasz Jachowicz");
        line2.setFont(AssetManager.getTextFont());
        line2.setTextFill(UIStyle.TEXT_COLOR);
        line2.setEffect(TEXT_SHADOW);

        Label line3 = new Label("Thanks for playing!");
        line3.setFont(AssetManager.getTextFont());
        line3.setTextFill(UIStyle.TEXT_COLOR);
        line3.setEffect(TEXT_SHADOW);

        ProfessionalButton menuBtn = new ProfessionalButton("MENU");
        menuBtn.setMinSize(240, 48);
        menuBtn.setOnAction(e -> {
            if (onBackToMenu != null) {
                cleanup();
                onBackToMenu.run();
            }
        });

        content.getChildren().addAll(title, line1, resultLabel, nameField, saveBtn, leaderboardBox, line2, line3, menuBtn);
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

    private void saveScore() {
        String name = LeaderboardStore.sanitizeName(nameField.getText());
        List<LeaderboardEntry> entries = LeaderboardStore.addEntry(new LeaderboardEntry(name, finalScore, finalTime));
        renderLeaderboard(entries);
        nameField.setText(name);
        nameField.setDisable(true);
        saveBtn.setEnabled(false);
    }

    private void renderLeaderboard(List<LeaderboardEntry> entries) {
        if (leaderboardBox == null) {
            return;
        }

        leaderboardBox.getChildren().clear();

        Label header = new Label("TOP 10");
        header.setFont(AssetManager.getHeadingFont());
        header.setTextFill(UIStyle.ACCENT_COLOR);
        header.setEffect(TEXT_SHADOW);
        leaderboardBox.getChildren().add(header);

        if (entries.isEmpty()) {
            Label empty = createLeaderboardLabel("No scores yet.");
            leaderboardBox.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);
            String row = String.format(
                    "%2d. %-16s %5d pts  %s",
                    i + 1,
                    entry.name(),
                    entry.score(),
                    entry.formattedTime()
            );
            leaderboardBox.getChildren().add(createLeaderboardLabel(row));
        }
    }

    private Label createLeaderboardLabel(String text) {
        Label label = new Label(text);
        label.setFont(AssetManager.getSmallFont());
        label.setTextFill(UIStyle.TEXT_COLOR);
        label.setEffect(TEXT_SHADOW);
        return label;
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
