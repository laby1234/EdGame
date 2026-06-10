package org.example.screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.audio.AudioManager;
import org.example.audio.AudioSettings;
import org.example.config.GameConfig;
import org.example.ui.AssetManager;
import org.example.ui.ProfessionalButton;
import org.example.ui.UIStyle;

import java.util.function.DoubleConsumer;

import static com.almasb.fxgl.dsl.FXGL.getGameScene;

public class SettingsScreen extends Screen {

    private static final DropShadow TITLE_SHADOW = createShadow(3, 3, 4, Color.web("#1C0F08"));
    private static final DropShadow TEXT_SHADOW = createShadow(1, 1, 2, Color.web("#1C0F08"));

    private StackPane rootPane;
    private final Runnable onBackCallback;

    public SettingsScreen(Runnable onBackCallback) {
        this.onBackCallback = onBackCallback;
    }

    @Override
    public void init() {
        rootPane = new StackPane();
        rootPane.setPrefWidth(GameConfig.WINDOW_WIDTH);
        rootPane.setPrefHeight(GameConfig.WINDOW_HEIGHT);

        Image bgImage = AssetManager.loadImage(AssetManager.SETTINGS_BG);
        if (bgImage != null) {
            ImageView bgView = new ImageView(bgImage);
            double imgW = bgImage.getWidth();
            double imgH = bgImage.getHeight();
            double scale = Math.max(
                    (double) GameConfig.WINDOW_WIDTH / imgW,
                    (double) GameConfig.WINDOW_HEIGHT / imgH
            );
            double fitW = imgW * scale;
            double fitH = imgH * scale;

            bgView.setPreserveRatio(true);
            bgView.setFitWidth(fitW);
            bgView.setSmooth(false);
            bgView.setTranslateX((GameConfig.WINDOW_WIDTH - fitW) / 2.0);
            bgView.setTranslateY((GameConfig.WINDOW_HEIGHT - fitH) / 2.0);
            rootPane.getChildren().add(bgView);
        } else {
            rootPane.setStyle(UIStyle.DARK_GRADIENT_BG);
        }

        rootPane.setOnKeyPressed(this::handleKeyPressed);
        rootPane.setFocusTraversable(true);

        VBox contentPane = createContentPane();
        rootPane.getChildren().add(contentPane);

        getGameScene().addUINode(rootPane);
        rootPane.requestFocus();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            if (onBackCallback != null) {
                cleanup();
                onBackCallback.run();
            }
            event.consume();
        }
    }

    private VBox createContentPane() {
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(50, 100, 50, 100));
        mainContainer.setStyle("-fx-background-color: transparent;");

        Label titleLabel = new Label("Ed Adventure");
        titleLabel.setFont(AssetManager.getTitleFont());
        titleLabel.setTextFill(UIStyle.ACCENT_COLOR);
        titleLabel.setEffect(TITLE_SHADOW);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        Label optionsLabel = new Label("SETTINGS");
        optionsLabel.setFont(AssetManager.getHeadingFont());
        optionsLabel.setTextFill(UIStyle.ACCENT_COLOR);
        optionsLabel.setEffect(TEXT_SHADOW);
        optionsLabel.setMaxWidth(Double.MAX_VALUE);
        optionsLabel.setAlignment(Pos.CENTER);

        VBox audioSection = createAudioSection();
        VBox graphicsSection = createGraphicsSection();

        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(30, 0, 0, 0));

        ProfessionalButton backBtn = new ProfessionalButton("BACK");
        backBtn.setMinSize(250, 50);
        backBtn.setOnAction(e -> {
            if (onBackCallback != null) {
                cleanup();
                onBackCallback.run();
            }
        });

        buttonContainer.getChildren().add(backBtn);

        mainContainer.getChildren().addAll(
                titleLabel,
                optionsLabel,
                audioSection,
                graphicsSection,
                buttonContainer
        );

        return mainContainer;
    }

    private VBox createAudioSection() {
        VBox section = new VBox(15);
        section.setStyle(UIStyle.PANEL_BORDER);
        section.setMaxWidth(500);

        Label sectionTitle = new Label("AUDIO SETTINGS");
        sectionTitle.setFont(AssetManager.getTextFont());
        sectionTitle.setTextFill(UIStyle.TEXT_COLOR);
        sectionTitle.setEffect(TEXT_SHADOW);

        HBox soundVolumeBox = createVolumeControl(
                "Sound Volume",
                AudioSettings.getSoundVolume(),
                AudioSettings::setSoundVolume
        );

        HBox musicVolumeBox = createVolumeControl(
                "Music Volume",
                AudioSettings.getMusicVolume(),
                value -> {
                    AudioSettings.setMusicVolume(value);
                    AudioManager.refreshMusicVolume();
                }
        );

        section.getChildren().addAll(sectionTitle, soundVolumeBox, musicVolumeBox);
        return section;
    }

    private VBox createGraphicsSection() {
        VBox section = new VBox(15);
        section.setStyle(UIStyle.PANEL_BORDER);
        section.setMaxWidth(500);

        Label sectionTitle = new Label("GRAPHICS SETTINGS");
        sectionTitle.setFont(AssetManager.getTextFont());
        sectionTitle.setTextFill(UIStyle.TEXT_COLOR);
        sectionTitle.setEffect(TEXT_SHADOW);

        Label resolutionLabel = new Label("Resolution: " + GameConfig.WINDOW_WIDTH + "x" + GameConfig.WINDOW_HEIGHT);
        resolutionLabel.setFont(AssetManager.getSmallFont());
        resolutionLabel.setTextFill(UIStyle.TEXT_COLOR);
        resolutionLabel.setEffect(TEXT_SHADOW);

        section.getChildren().addAll(sectionTitle, resolutionLabel);
        return section;
    }

    private HBox createVolumeControl(String label, double initialValue, DoubleConsumer onChange) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);

        Label labelControl = new Label(label);
        labelControl.setFont(AssetManager.getSmallFont());
        labelControl.setTextFill(UIStyle.TEXT_COLOR);
        labelControl.setEffect(TEXT_SHADOW);
        labelControl.setMinWidth(150);

        Slider slider = new Slider(0, 1, initialValue);
        slider.setStyle(UIStyle.SLIDER_STYLE);
        slider.setPrefWidth(250);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);

        Label valueLabel = new Label(String.format("%.0f%%", initialValue * 100));
        valueLabel.setFont(AssetManager.getSmallFont());
        valueLabel.setTextFill(UIStyle.TEXT_COLOR);
        valueLabel.setEffect(TEXT_SHADOW);
        valueLabel.setMinWidth(50);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double value = newVal.doubleValue();
            valueLabel.setText(String.format("%.0f%%", value * 100));
            onChange.accept(value);
        });

        box.getChildren().addAll(labelControl, slider, valueLabel);
        return box;
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
