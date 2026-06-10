package org.example.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ProfessionalButton extends VBox {
    private final Button button;

    public ProfessionalButton(String text) {
        this(text, null);
    }

    public ProfessionalButton(String text, String iconPath) {
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: transparent;");
        setSpacing(0);

        button = new Button(text);
        String normalUrl = getResourceUrl(AssetManager.BTN_NORMAL);
        String hoverUrl = getResourceUrl(AssetManager.BTN_HOVER);
        String pressedUrl = getResourceUrl(AssetManager.BTN_PRESSED);

        button.setMinWidth(300);
        button.setMinHeight(60);
        button.setFont(AssetManager.getTextFont());

        if (normalUrl != null && hoverUrl != null && pressedUrl != null) {
            applyImageStyles(normalUrl, hoverUrl, pressedUrl);
        } else {
            applyFallbackStyles();
        }

        addIcon(iconPath);

        getChildren().add(button);
    }

    public void setOnAction(EventHandler<ActionEvent> handler) {
        button.setOnAction(handler);
    }

    public Button getButton() {
        return button;
    }

    public void setText(String text) {
        button.setText(text);
    }

    public String getText() {
        return button.getText();
    }

    public void setEnabled(boolean enabled) {
        button.setDisable(!enabled);
        if (!enabled) {
            button.setStyle(UIStyle.BUTTON_BASE +
                    " -fx-padding: 12px 40px; " +
                    " -fx-text-fill: #696969; " +
                    " -fx-background-color: linear-gradient(to bottom, #4a4a4a 0%, #2a2a2a 100%); " +
                    " -fx-border-width: 3; " +
                    " -fx-border-color: #696969 #2a2a2a #2a2a2a #696969;");
        } else {
            button.setStyle(UIStyle.BUTTON_NORMAL);
        }
    }

    public void setMinSize(double width, double height) {
        button.setMinWidth(width);
        button.setMinHeight(height);
    }

    private void applyImageStyles(String normalUrl, String hoverUrl, String pressedUrl) {
        String baseStyle = "-fx-background-color: transparent; -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-size: contain;";
        String normalStyle = baseStyle + " -fx-background-image: url('" + normalUrl + "');";
        String hoverStyle = baseStyle + " -fx-background-image: url('" + hoverUrl + "');";
        String pressedStyle = baseStyle + " -fx-background-image: url('" + pressedUrl + "');";

        button.setStyle(normalStyle);
        applyMouseStyles(normalStyle, hoverStyle, pressedStyle);
    }

    private void applyFallbackStyles() {
        button.setStyle(UIStyle.BUTTON_NORMAL);
        applyMouseStyles(UIStyle.BUTTON_NORMAL, UIStyle.BUTTON_HOVER, UIStyle.BUTTON_PRESSED);
    }

    private void applyMouseStyles(String normalStyle, String hoverStyle, String pressedStyle) {
        button.setOnMouseEntered(e -> {
            button.setStyle(hoverStyle);
            button.setTranslateY(-2);
            button.setTranslateX(0);
        });

        button.setOnMouseExited(e -> {
            button.setStyle(normalStyle);
            button.setTranslateY(0);
            button.setTranslateX(0);
        });

        button.setOnMousePressed(e -> {
            button.setStyle(pressedStyle);
            button.setTranslateY(2);
        });

        button.setOnMouseReleased(e -> {
            button.setStyle(hoverStyle);
            button.setTranslateY(-2);
        });
    }

    private void addIcon(String iconPath) {
        if (iconPath == null || iconPath.isEmpty()) {
            return;
        }

        try {
            Image iconImage = new Image(iconPath);
            ImageView iconView = new ImageView(iconImage);
            iconView.setFitWidth(24);
            iconView.setFitHeight(24);
            button.setGraphic(iconView);
        } catch (RuntimeException e) {
            System.err.println("Could not load icon: " + iconPath + " - " + e.getMessage());
        }
    }

    private String getResourceUrl(String resourcePath) {
        try {
            java.net.URL resource = AssetManager.class.getResource("/" + resourcePath);
            return resource == null ? null : resource.toExternalForm();
        } catch (RuntimeException e) {
            System.err.println("Could not resolve button resource: " + resourcePath + " - " + e.getMessage());
            return null;
        }
    }
}

