package org.example.screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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

public class PauseScreen extends Screen {

    private static final DropShadow TITLE_SHADOW = createShadow(3, 3, 4, Color.web("#1C0F08"));
    private static final DropShadow TEXT_SHADOW = createShadow(1, 1, 2, Color.web("#1C0F08"));

    private StackPane rootPane;
    private final Runnable onResumeCallback;
    private final Runnable onMenuCallback;
    private boolean showingOptions = false;

    public PauseScreen(Runnable onResumeCallback, Runnable onMenuCallback) {
        this.onResumeCallback = onResumeCallback;
        this.onMenuCallback = onMenuCallback;
    }

    @Override
    public void init() {
        rootPane = new StackPane();
        rootPane.setPrefWidth(GameConfig.WINDOW_WIDTH);
        rootPane.setPrefHeight(GameConfig.WINDOW_HEIGHT);

        rootPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        rootPane.setOnKeyPressed(this::handleKeyPressed);
        rootPane.setFocusTraversable(true);
        rootPane.requestFocus();

        VBox pausePanel = createPausePanel();
        rootPane.getChildren().add(pausePanel);

        getGameScene().addUINode(rootPane);
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && !showingOptions) {
            if (onResumeCallback != null) {
                cleanup();
                onResumeCallback.run();
            }
            event.consume();
        }
    }

    private VBox createPausePanel() {
        VBox panel = new VBox(30);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(50));
        panel.setStyle("-fx-background-color: transparent;");

        Label pauseLabel = new Label("Ed Adventure");
        styleTitleLabel(pauseLabel);

        Label pauseStatusLabel = new Label("PAUSED");
        styleHeadingLabel(pauseStatusLabel);

        VBox buttonBox = new VBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(30, 0, 0, 0));

        ProfessionalButton resumeBtn = new ProfessionalButton("RESUME");
        resumeBtn.setMinSize(300, 60);
        resumeBtn.setOnAction(e -> {
            if (onResumeCallback != null) {
                cleanup();
                onResumeCallback.run();
            }
        });

        ProfessionalButton optionsBtn = new ProfessionalButton("OPTIONS");
        optionsBtn.setMinSize(300, 60);
        optionsBtn.setOnAction(e -> {
            showingOptions = true;
            showSettingsPanel();
        });

        ProfessionalButton exitBtn = new ProfessionalButton("EXIT");
        exitBtn.setMinSize(300, 60);
        exitBtn.setOnAction(e -> {
            if (onMenuCallback != null) {
                cleanup();
                onMenuCallback.run();
            }
        });

        buttonBox.getChildren().addAll(resumeBtn, optionsBtn, exitBtn);

        panel.getChildren().addAll(pauseLabel, pauseStatusLabel, buttonBox);

        return panel;
    }

    private void showSettingsPanel() {
        rootPane.getChildren().clear();

        rootPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        VBox settingsPanel = new VBox(30);
        settingsPanel.setAlignment(Pos.TOP_CENTER);
        settingsPanel.setPadding(new Insets(50, 100, 50, 100));
        settingsPanel.setStyle("-fx-background-color: transparent;");

        Label titleLabel = new Label("Ed Adventure");
        styleTitleLabel(titleLabel);

        Label optionsLabel = new Label("OPTIONS");
        styleHeadingLabel(optionsLabel);

        VBox audioSection = createAudioSection();

        VBox graphicsSection = createGraphicsSection();

        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(30, 0, 0, 0));

        ProfessionalButton backBtn = new ProfessionalButton("BACK");
        backBtn.setMinSize(250, 50);
        backBtn.setOnAction(e -> {
            showingOptions = false;
            rootPane.getChildren().clear();
            rootPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
            VBox pausePanel = createPausePanel();
            rootPane.getChildren().add(pausePanel);
            rootPane.requestFocus();
        });

        buttonContainer.getChildren().add(backBtn);

        settingsPanel.getChildren().addAll(
                titleLabel,
                optionsLabel,
                audioSection,
                graphicsSection,
                buttonContainer
        );

        rootPane.getChildren().add(settingsPanel);
        rootPane.requestFocus();
    }

    private VBox createAudioSection() {
        VBox section = new VBox(15);
        section.setStyle(UIStyle.PANEL_BORDER);
        section.setMaxWidth(500);

        Label sectionTitle = new Label("AUDIO SETTINGS");
        styleSectionLabel(sectionTitle);

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
        styleSectionLabel(sectionTitle);

        Label resolutionLabel = new Label("Resolution: " + GameConfig.WINDOW_WIDTH + "x" + GameConfig.WINDOW_HEIGHT);
        styleSmallLabel(resolutionLabel);

        Label fpsLabel = new Label("FPS: 60");
        styleSmallLabel(fpsLabel);

        section.getChildren().addAll(sectionTitle, resolutionLabel, fpsLabel);

        return section;
    }

    private HBox createVolumeControl(String label, double initialValue, DoubleConsumer onChange) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);

        Label labelControl = new Label(label);
        styleSmallLabel(labelControl);
        labelControl.setMinWidth(150);

        Slider slider = new Slider(0, 1, initialValue);
        slider.setStyle(UIStyle.SLIDER_STYLE);
        slider.setPrefWidth(250);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);

        Label valueLabel = new Label(String.format("%.0f%%", initialValue * 100));
        styleSmallLabel(valueLabel);
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

    private void styleTitleLabel(Label label) {
        label.setFont(AssetManager.getTitleFont());
        label.setTextFill(UIStyle.ACCENT_COLOR);
        label.setEffect(TITLE_SHADOW);
    }

    private void styleHeadingLabel(Label label) {
        label.setFont(AssetManager.getHeadingFont());
        label.setTextFill(UIStyle.ACCENT_COLOR);
        label.setEffect(TEXT_SHADOW);
    }

    private void styleSectionLabel(Label label) {
        label.setFont(AssetManager.getTextFont());
        label.setTextFill(UIStyle.TEXT_COLOR);
        label.setEffect(TEXT_SHADOW);
    }

    private void styleSmallLabel(Label label) {
        label.setFont(AssetManager.getSmallFont());
        label.setTextFill(UIStyle.TEXT_COLOR);
        label.setEffect(TEXT_SHADOW);
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



